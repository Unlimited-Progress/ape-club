**Async 入口**
- `d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\executor\Async.java`
- 公共方法概览（签名 + 目的）
    - `public static boolean beginWork(long timeout, ExecutorService executorService, List<WorkerWrapper> workerWrappers)` — 执行入口（批量），阻塞等待全部完成或超时；超时会收集所有任务并调用 `stopNow` 中断链路。`Async` 内部持有静态 `executorService`。`[30–54]`
    - `public static boolean beginWork(long timeout, ExecutorService executorService, WorkerWrapper... workerWrapper)` — 重载（变参），委派到上面的 List 版本。`[59–65]`
    - `public static boolean beginWork(long timeout, WorkerWrapper... workerWrapper)` — 使用默认线程池 `COMMON_POOL` 执行，阻塞。`[70–72]`
    - `public static void beginWorkAsync(long timeout, IGroupCallback groupCallback, WorkerWrapper... workerWrapper)` — 异步执行（默认线程池），完成后触发 `IGroupCallback` 的 `success`/`failure`。`[74–76]`
    - `public static void beginWorkAsync(long timeout, ExecutorService executorService, IGroupCallback groupCallback, WorkerWrapper... workerWrapper)` — 异步执行（可指定线程池），包裹回调；若未提供回调则使用 `DefaultGroupCallback`。`[81–116]`
    - `public static void shutDown()` — 关闭持有的静态线程池或默认池的便捷方法（委派到重载）。`[137–139]`
    - `public static void shutDown(ExecutorService executorService)` — 安全关闭指定线程池；为 `null` 时关闭默认池。`[144–150]`
    - `public static String getThreadCount()` — 返回默认池的活跃、完成、最大线程数统计信息。`[152–156]`
- 线程池说明与关停
    - 默认池：`private static final ThreadPoolExecutor COMMON_POOL = ...`（`newCachedThreadPool`）。`[21]`
    - 自定义池：通过 `beginWork(..., ExecutorService, ...)` 注入，并在 `Async.executorService` 静态字段中保存。`[25,35]`
    - 关停：优先关闭传入的池；未传入则关闭默认池。`[144–150]`
- 超时与中断
    - `beginWork` 使用 `CompletableFuture.allOf(...).get(timeout, TimeUnit.MILLISECONDS)` 控制超时；超时捕获后遍历全链路 `WorkerWrapper` 执行 `stopNow`。`[44–53,121–132]`

**WorkerWrapper 构建器**
- `d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\wrapper\WorkerWrapper.java`
- Builder 选项与方法（签名 + 用法）
    - `public static class Builder<W, C>` 定义与字段：`id`（默认 `UUID`）、`param`、`worker`、`callback`、`nextWrappers`、`dependWrappers`、`selfIsMustSet`、`needCheckNextWrapperResult`。`[480–506]`
    - `public Builder<W, C> worker(IWorker<W, C> worker)` — 指定执行单元，必填；否则构造器抛 `NullPointerException`。`[506–509,81–85]`
    - `public Builder<W, C> param(W w)` — 指定入参。`[511–514]`
    - `public Builder<W, C> id(String id)` — 指定唯一标识（可选）。`[516–521]`
    - `public Builder<W, C> callback(ICallback<W, C> callback)` — 指定回调（可选，默认 `DefaultCallback`）。`[528–531,89–93]`
    - `public Builder<W, C> needCheckNextWrapperResult(boolean needCheckNextWrapperResult)` — 是否在执行前检查下游是否已开始或完成（避免重复/无效执行；仅当 `nextWrappers.size() <= 1` 有效）。`[523–526,67–75,117–126,452–454]`
    - `public Builder<W, C> depend(WorkerWrapper<?, ?>... wrappers)` — 声明依赖（默认强依赖），支持变参。`[533–541,543–546]`
    - `public Builder<W, C> depend(WorkerWrapper<?, ?> wrapper, boolean isMust)` — 声明依赖并指定是否“必须完成”后才能执行自己。`[547–557]`
    - `public Builder<W, C> next(WorkerWrapper<?, ?> wrapper)` — 声明后继（默认后继强依赖于自己）。`[559–561]`
    - `public Builder<W, C> next(WorkerWrapper<?, ?> wrapper, boolean selfIsMust)` — 声明后继，并指定“后继是否强依赖自己”。`[563–577]`
    - `public Builder<W, C> next(WorkerWrapper<?, ?>... wrappers)` — 批量声明后继（默认强依赖）。`[579–587]`
    - `public WorkerWrapper<W, C> build()` — 构建：回填双向关系（为后继添加对当前的 `depend`，并标记 `must`）；应用 `needCheckNextWrapperResult`。`[589–610]`
- 强依赖标识与行为
    - 依赖方“必须完成”标识：`DependWrapper.must` 控制当存在多个上游时是否需要全部完成才能执行自己。`[547–557]; DependWrapper [26,44–50]`
    - 后继“强依赖自己”标识：`next(wrapper, selfIsMust)` 将后继加入 `selfIsMustSet`，在 `build` 时为后继添加对当前的依赖并设置 `must=true`。`[563–577,599–606]`
- 运行与状态
    - 执行入口（内部）：`public void work(ExecutorService, long remainTime, Map<String, WorkerWrapper> all)`，处理依赖、调度后继、超时快速失败。`[151–153,96–148,182–203]`
    - 停止：`public void stopNow()` 在总超时下将状态置为失败并回调。`[158–162,303–320]`
    - 结果：`private WorkResult<V> workResult` 初始为 `WorkResult.defaultResult()`；成功置 `SUCCESS`，超时置 `TIMEOUT`，异常置 `EXCEPTION`。`[65,346–351,426–437]`
    - 状态机：`INIT(0) → WORKING(3) → FINISH(1)` 或 `ERROR(2)`；通过 `compareAndSetState` 保证不会重复执行。`[76–80,332–345,303–320,448–450]`

**Worker/Callback 接口**
- `d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\callback\IWorker.java`
    - `@FunctionalInterface public interface IWorker<T, V>` — 执行单元接口。`[12–13]`
    - `V action(T object, Map<String, WorkerWrapper> allWrappers)` — 业务执行方法；可从 `allWrappers` 取其他节点结果。`[20]`
    - `default V defaultValue()` — 超时/异常时的默认返回。`[27–29]`
- `d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\callback\ICallback.java`
    - `@FunctionalInterface public interface ICallback<T, V>` — 回调接口。`[12–13]`
    - `default void begin()` — 任务开始钩子。`[18–20]`
    - `void result(boolean success, T param, WorkResult<V> workResult)` — 结果回调；`success=false` 时包含超时或异常信息。`[25]`

**结果类型**
- `d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\worker\WorkResult.java`
    - 字段：`private V result`、`private ResultState resultState`、`private Exception ex`。`[10,14–16]`
    - 构造与默认：`new WorkResult(result, state, ex)`；`defaultResult()` 置 `ResultState.DEFAULT`。`[17–25,27–29]`
    - 访问器：`getResult/setResult`、`getResultState/setResultState`、`getEx/setEx`。`[48–62,40–46]`
- `d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\worker\ResultState.java`
    - `public enum ResultState { SUCCESS, TIMEOUT, EXCEPTION, DEFAULT }` — 结果枚举。`[7–12]`
- `d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\worker\DependWrapper.java`
    - 依赖封装：`private WorkerWrapper<?, ?> dependWrapper; private boolean must`；控制是否“必须完成”。`[11–31,36–50]`

**使用模式与注意**
- 构建任务链
    - 先为每个节点用 `new WorkerWrapper.Builder<In, Out>().id(...).param(...).worker(...).callback(...).build()` 创建。`[480–506,506–531,589–610]`
    - 用 `.depend(...)` 设置上游（默认强依赖）；或 `.depend(wrapper, false)` 表示“任意一个完成即可”。`[533–557]`
    - 用 `.next(...)` 设置后继；`next(wrapper, true)` 表示“后继强依赖于当前”。`[559–577]`
    - 对于最多一个后继的链路，可通过 `.needCheckNextWrapperResult(false)` 关闭“执行前检查下游”以避免被下游抢先执行而短路。`[523–526,67–75,117–126]`
- 执行与回调
    - 阻塞执行：`Async.beginWork(timeout, wrappers...)`；返回 `true/false` 表示是否在超时内全部完成。`[70–72]`
    - 异步执行：`Async.beginWorkAsync(timeout, groupCallback, wrappers...)`；回调 `IGroupCallback.success/failure`。`[74–116]`
    - 超时处理：超时后链路中所有 `WorkerWrapper.stopNow()`，下游会收到 `WorkResult` 的 `ResultState.TIMEOUT` 与 `defaultValue()`。`[44–53,158–162,426–431]`
- 实现细节与坑
    - `IWorker.action(...)` 内若抛异常，`ResultState` 置为 `EXCEPTION`，并回调 `success=false`；确保 `defaultValue()` 返回可用的降级值。`[352–359,432–437]`
    - 状态原子变更防止重复执行；并发依赖场景通过 `must` 集合保证全部必需完成后再触发自己。`[332–345,255–289]`
    - `needCheckNextWrapperResult` 仅在后继数量为 1 时生效；多后继并行场景不会进行该“短路检查”。`[169–177]`
- 关闭线程池
    - 用 `Async.shutDown()` 或 `Async.shutDown(executor)` 释放资源；若使用默认池，亦需在应用生命周期末尾调用。`[137–150]`


toolName: agent_finish

status: success


**模块作用**
- `asyncTool`在项目中充当异步编排的基础库：封装并行/串行执行、任务依赖、组级超时与结果回调。
- 当前只有模块自身在用；它提供一套可在其他子模块或外部项目复用的API与线程池管理。

**核心API**
- `Async`（入口执行器）：`d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\executor\Async.java`
    - `beginWork(long timeout, WorkerWrapper... wrappers)` 阻塞执行，组超时控制 `asyncTool/.../Async.java:70-72`
    - `beginWork(long timeout, ExecutorService exec, List<WorkerWrapper> wrappers)` 自定义线程池与批量执行 `asyncTool/.../Async.java:30-54`
    - `beginWorkAsync(long timeout, IGroupCallback cb, WorkerWrapper... wrappers)` 异步执行并触发分组回调 `asyncTool/.../Async.java:74-76,81-116`
    - `shutDown()`/`shutDown(ExecutorService)` 关闭默认或指定线程池 `asyncTool/.../Async.java:137-150`
    - `getThreadCount()` 线程池状态统计 `asyncTool/.../Async.java:152-156`

- `WorkerWrapper`（任务封装与依赖编排）：`d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\wrapper\WorkerWrapper.java`
    - `Builder.worker(IWorker)` 设置执行单元（必填） `asyncTool/.../WorkerWrapper.java:506-509`
    - `Builder.param(T)` 设置入参 `asyncTool/.../WorkerWrapper.java:511-514`
    - `Builder.id(String)` 设置唯一标识 `asyncTool/.../WorkerWrapper.java:516-521`
    - `Builder.callback(ICallback)` 设置回调（可选） `asyncTool/.../WorkerWrapper.java:528-531`
    - `Builder.depend(wrapper, boolean isMust)` 声明上游依赖及“必须完成”标识 `asyncTool/.../WorkerWrapper.java:547-557`
    - `Builder.next(wrapper, boolean selfIsMust)` 声明后继并指定“后继强依赖自己” `asyncTool/.../WorkerWrapper.java:563-577`
    - `Builder.needCheckNextWrapperResult(boolean)` 单后继场景执行前检查短路 `asyncTool/.../WorkerWrapper.java:523-526`
    - `build()` 双向回填依赖关系，应用强依赖标识 `asyncTool/.../WorkerWrapper.java:589-610`

- `IWorker`/`ICallback`（业务与回调）：`d:\IDEA_Workspace\IDEA_Project\jc-club\asyncTool\src\main\java\com\jd\platform\async\callback\IWorker.java`、`ICallback.java`
    - `IWorker.action(T param, Map<String, WorkerWrapper> all)` 执行业务逻辑 `asyncTool/.../IWorker.java:20`
    - `IWorker.defaultValue()` 超时/异常时的降级返回值 `asyncTool/.../IWorker.java:27-29`
    - `ICallback.begin()` 开始钩子 `asyncTool/.../ICallback.java:18-20`
    - `ICallback.result(boolean success, T param, WorkResult<V> res)` 完成/失败回调 `asyncTool/.../ICallback.java:25`

- 结果类型与枚举：
    - `WorkResult<V>`：结果、状态、异常 `asyncTool/.../worker/WorkResult.java:10,14-16,17-25,48-62`
    - `ResultState`：`SUCCESS/TIMEOUT/EXCEPTION/DEFAULT` `asyncTool/.../worker/ResultState.java:7-12`

**如何使用（最小流程）**
- 实现一个或多个`IWorker`与可选的`ICallback`。
- 为每个任务构建`WorkerWrapper`：
    - 使用`Builder`设置`id/param/worker/callback`并`build`。
    - 用`.depend(...)`声明上游依赖；用`.next(...)`声明后继及强依赖。
- 执行：
    - 阻塞：`Async.beginWork(timeoutMillis, w1, w2, ...)`。
    - 异步：`Async.beginWorkAsync(timeoutMillis, groupCallback, w1, w2, ...)`。
- 结束时关闭线程池：`Async.shutDown()`或关闭自定义`ExecutorService`。

**典型用法参考（测试代码）**
- 并行与超时：`asyncTool/src/test/java/parallel/TestPar.java:67-73,111-116,152-158,193-199`
- 依赖编排：`asyncTool/src/test/java/parallel/TestPar.java:101-107,139-141`；`asyncTool/src/test/java/dependnew/Test.java:31-40`
- 结果传递：`asyncTool/src/test/java/depend/Test.java:45-48`；`asyncTool/src/test/java/depend/LambdaTest.java:64-68`
- 串行链路：`asyncTool/src/test/java/seq/TestSequential.java:33-41,52-70`
- 回调绑定：`asyncTool/src/test/java/dependnew/Test.java:22-40`；`asyncTool/src/test/java/parallel/TestPar.java:47-61`

**注意事项**
- 为每个`WorkerWrapper`设置`IWorker`是必需项；否则构建时抛错。
- 超时会触发全链路中断：下游收到`ResultState.TIMEOUT`并返回`defaultValue()`。
- `.needCheckNextWrapperResult(true)`仅在单后继场景有效，用于避免被下游抢先执行而短路。
- 使用默认线程池时，应用退出前调用`Async.shutDown()`释放资源；使用自定义池时自行管理生命周期。

如果你希望我给出一段在项目其他子模块中直接复用的最小示例（并行或依赖场景），我可以按上述API写好示例类与`pom.xml`依赖修改方案，并标注放置路径。