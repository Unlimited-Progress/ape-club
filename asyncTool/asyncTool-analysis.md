# asyncTool 项目技术文档

## 项目概述与作用

### 项目定位

asyncTool 是 JC-Club 平台中的异步任务编排框架，专注于解决复杂业务流程中的异步执行、任务依赖管理、超时控制等核心问题。该项目采用 Java 语言开发，提供了一套简洁而强大的 API，允许开发者以声明式的方式构建异步任务执行图，实现任务的并行执行、串行执行以及复杂的依赖关系编排。作为平台基础设施层的重要组成部分，asyncTool 为上层业务模块提供了统一的异步任务处理能力，有效提升了系统的并发性能和响应效率。当前项目版本为 1.4.1-SNAPSHOT，基于 Maven 进行依赖管理，要求 Java 1.8 及以上运行环境。该框架的设计理念是让异步编程变得简单可控，通过包装器模式和 CompletableFuture 技术，将底层的线程调度、任务协调、超时处理等复杂逻辑封装在框架内部，为开发者提供清晰直观的使用接口。

### 核心价值

asyncTool 为 JC-Club 平台带来了多方面的核心价值。首先，它提供了强大的任务编排能力，支持串行、并行、混合等多种执行模式，开发者可以根据业务需求灵活构建任务执行拓扑结构，满足复杂业务流程的异步化改造需求。其次，框架内置了完善的依赖管理机制，通过强依赖和弱依赖两种模式，可以精确控制任务之间的执行顺序和触发条件，避免了传统异步编程中容易出现的竞态条件和数据一致性问题。再者，asyncTool 实现了细粒度的超时控制，支持整体超时和单任务超时两个层面，超时后会触发预设的降级逻辑，确保系统不会因为单个任务的阻塞而整体瘫痪。此外，框架提供了优雅的回调机制，无论是单任务级别的结果回调还是整组任务完成后的组回调，都能够方便地集成到业务代码中，实现异步任务执行过程的全程可观测。最后，通过对线程池的统一管理，asyncTool 有效控制了系统的并发资源消耗，避免了无限制创建线程导致的资源耗尽问题，同时支持自定义线程池以满足特定场景的性能调优需求。

### 解决的核心问题

asyncTool 的设计目标是解决传统异步编程中的一系列痛点问题。在并发控制方面，传统方案往往需要开发者手动管理线程数量和任务调度，代码复杂度高且容易出错，asyncTool 通过 WorkerWrapper 包装器和统一的执行入口，将这些复杂性封装在框架内部，开发者只需关注业务逻辑本身。在依赖处理方面，当存在多个任务之间的依赖关系时，如何确保前置任务完成后才启动后续任务、如何处理部分依赖完成就触发的场景，这些都是异步编程中的常见难题，asyncTool 通过 DependWrapper 和 must 标识提供了清晰直观的解决方案。在超时处理方面，分布式环境下网络抖动和服务延迟是常态，单个任务的长时间阻塞会影响整体响应，asyncTool 实现了基于 CompletableFuture 的超时检测机制，能够在指定时间内快速失败并触发降级逻辑。在结果传递方面，异步任务执行完毕后如何将结果传递给后续任务使用，如何处理成功、失败、超时等多种结果状态，asyncTool 通过 WorkResult 和 ICallback 机制提供了统一的结果处理框架。这些问题的解决使得 asyncTool 成为构建高可靠、高性能异步系统的理想选择。

### 应用场景

asyncTool 适用于多种需要异步处理的业务场景。在微服务调用场景中，当一个业务请求需要调用多个下游服务时，可以将这些调用封装为独立的异步任务并行执行，大幅缩短整体响应时间，同时通过依赖管理确保数据整合的正确性。在数据处理场景中，当需要执行多阶段的数据清洗、转换、分析任务时，可以使用串行依赖模式确保各阶段按序执行，也可以使用并行模式同时处理独立的数据分片。在事件驱动场景中，当一个业务事件需要触发多个后续处理动作时，可以使用广播模式让所有后继任务并行响应，也可以使用链式模式让任务按特定顺序依次执行。在批处理场景中，当需要处理大量独立但又存在某种关联的数据项时，asyncTool 可以有效地组织任务执行顺序，充分利用系统资源提升处理效率。在降级熔断场景中，当某些非核心功能超时或失败时，通过预设的降级逻辑返回默认值，确保核心业务不受影响。这些场景充分展示了 asyncTool 在实际业务开发中的广泛适用性。

## 核心功能

### 任务执行入口

Async 类是 asyncTool 框架的核心入口类，提供了多种任务执行方式的静态方法。该类采用单例模式管理线程池资源，通过静态字段 COMMON_POOL 和 executorService 维护执行环境。beginWork 方法是主要的同步执行入口，支持三种调用形式：指定超时时间、自定义线程池和任务包装器列表的完整形式；省略线程池参数使用默认线程池的简化形式；以及接收变长参数的可变参形式。执行过程中，框架会创建 CompletableFuture 来包装每个任务的异步执行，通过 allOf 方法等待全部完成或超时。超时后会自动调用 stopNow 方法停止所有相关任务，确保系统资源的及时释放。对于需要非阻塞执行的场景，beginWorkAsync 方法提供了异步执行能力，执行完成后通过 IGroupCallback 接口回调通知调用方结果。shutDown 方法用于优雅关闭线程池，getThreadCount 方法提供了线程池状态的监控能力。

```java
// 同步执行入口
public static boolean beginWork(long timeout, ExecutorService executorService, 
                                 List<WorkerWrapper> workerWrappers) throws ExecutionException, InterruptedException

// 使用默认线程池的简化调用
public static boolean beginWork(long timeout, WorkerWrapper... workerWrapper) throws ExecutionException, InterruptedException

// 异步执行并组回调
public static void beginWorkAsync(long timeout, IGroupCallback groupCallback, WorkerWrapper... workerWrapper)

// 关闭线程池
public static void shutDown()
public static void shutDown(ExecutorService executorService)

// 获取线程池状态
public static String getThreadCount()
```

### 任务包装器

WorkerWrapper 是 asyncTool 中最重要的核心类，它对每个执行单元（IWorker）和回调（ICallback）进行一对一封装，是构建任务依赖图的基本单元。每个 WorkerWrapper 具有唯一的标识 id、待处理的参数 param、核心执行逻辑 worker、结果回调 callback、下游任务列表 nextWrappers、上游依赖列表 dependWrappers 等关键属性。框架通过 Builder 模式提供灵活的构建方式，必填属性只有 worker，可选包括 param、callback、id 等。依赖关系的设置支持链式声明，通过 depend 方法指定前置任务，通过 next 方法指定后继任务。needCheckNextWrapperResult 属性用于控制是否在执行前检查下游状态，避免重复执行。WorkerWrapper 内部维护了原子状态机（INIT、WORKING、FINISH、ERROR），确保任务不会重复执行，同时通过 checkNextWrapperResult 方法实现执行链路的智能剪枝，避免无效计算。

```java
// WorkerWrapper 构建示例
WorkerWrapper.Builder<Input, Output> builder = new WorkerWrapper.Builder<Input, Output>();
builder.id("unique-id")
       .param(inputData)
       .worker(new MyWorker())
       .callback(new MyCallback())
       .depend(previousWrapper1, previousWrapper2)
       .next(nextWrapper)
       .needCheckNextWrapperResult(true);
WorkerWrapper<Input, Output> wrapper = builder.build();
```

### 执行单元接口

IWorker 接口是异步任务执行单元的定义规范，采用 @FunctionalInterface 注解标记，可以直接使用 Lambda 表达式简化实现。该接口包含两个核心方法：action 方法是任务执行的主逻辑，接收输入参数和所有任务包装器的映射，可在此方法中访问其他并行任务的执行结果；defaultValue 方法提供超时或异常时的默认返回值，用于降级处理。框架还提供了 ITimeoutWorker 接口扩展 IWorker，增加了 timeOut 方法定义单任务超时时间和 enableTimeOut 方法控制是否启用单任务超时检测。需要注意的是，启用单任务超时检测会增加线程池资源消耗，因为需要在单独的超时监控线程中管理计时器。这种设计允许在组级别超时和单任务级别超时之间进行灵活的权衡配置，满足不同业务场景的需求。

```java
// 执行单元接口定义
@FunctionalInterface
public interface IWorker<T, V> {
    V action(T object, Map<String, WorkerWrapper> allWrappers);
    default V defaultValue() { return null; }
}

// 带超时的执行单元
public interface ITimeoutWorker<T, V> extends IWorker<T, V> {
    long timeOut();
    boolean enableTimeOut();
}

// 使用示例
IWorker<String, Integer> worker = (param, allWrappers) -> {
    // 执行业务逻辑
    Integer result = doBusiness(param);
    return result;
};
```

### 结果回调机制

ICallback 接口定义了任务执行结果的回调规范，采用 @FunctionalInterface 注解标记。回调接口包含两个方法：begin 方法是一个钩子函数，在任务开始执行前被调用，可用于记录开始时间或执行前置处理；result 方法在任务执行完毕后被调用，接收 success 标识、原始参数和 WorkResult 结果对象。WorkResult 封装了任务的执行结果，包含实际结果值 result、结果状态 ResultState 和可能的异常信息 ex。ResultState 枚举定义了四种状态：SUCCESS 表示正常完成，TIMEOUT 表示超时，EXCEPTION 表示执行异常，DEFAULT 表示初始默认状态。框架还提供了 DefaultCallback 作为默认实现，当用户未指定回调时会自动使用，避免空指针异常。这种设计实现了执行逻辑与结果处理的分离，使业务代码更加清晰。

```java
// 结果回调接口
@FunctionalInterface
public interface ICallback<T, V> {
    default void begin() {}
    void result(boolean success, T param, WorkResult<V> workResult);
}

// 使用示例
ICallback<String, Integer> callback = new ICallback<String, Integer>() {
    @Override
    public void begin() {
        System.out.println("任务开始执行");
    }
    
    @Override
    public void result(boolean success, String param, WorkResult<Integer> workResult) {
        if (success) {
            System.out.println("执行成功，结果：" + workResult.getResult());
        } else {
            System.out.println("执行失败，状态：" + workResult.getResultState());
        }
    }
};
```

### 依赖管理机制

依赖管理是 asyncTool 的核心特性之一，通过 DependWrapper 类封装依赖关系。DependWrapper 包含被依赖的 WorkerWrapper 引用和 must 布尔标识，must=true 表示强依赖，必须等所有强依赖任务完成后才能执行当前任务；must=false 表示弱依赖，任意一个弱依赖任务完成后即可触发当前任务。这种设计支持两种典型的依赖模式：AND 模式下所有前置任务必须全部完成才执行后续任务，适用于需要聚合多路数据后继续处理的场景；OR 模式下任意一个前置任务完成即可触发后续任务，适用于只需等待最先完成结果的场景。在 WorkerWrapper 构建过程中，build 方法会自动维护依赖的双向关系，为每个后继任务添加对当前任务的依赖引用，并设置相应的 must 标识，确保依赖关系的正确传递。框架还支持复杂的依赖图结构，可以构建树形、网状等任意拓扑结构的任务执行图。

```java
// 依赖管理类
public class DependWrapper {
    private WorkerWrapper<?, ?> dependWrapper;
    private boolean must;  // true=必须全部完成，false=任意一个完成即可
}

// 依赖设置示例
WorkerWrapper.Builder<Input, Output> builder = new WorkerWrapper.Builder<>();
// 强依赖：必须等wrapper1和wrapper2都完成后才能执行
builder.depend(wrapper1, wrapper2);
// 弱依赖：wrapper1或wrapper2任一完成即可执行
builder.depend(wrapper3, false);
```

### 超时控制机制

asyncTool 提供了两个层面的超时控制能力：组级别超时和单任务级别超时。组级别超时通过 beginWork 方法的 timeout 参数指定，控制整个任务图的执行时间。实现上使用 CompletableFuture.allOf(...).get(timeout, TimeUnit.MILLISECONDS) 阻塞等待，超时后会抛出 TimeoutException，框架捕获异常后遍历所有 WorkerWrapper 调用 stopNow 方法停止执行。单任务级别超时通过 ITimeoutWorker 接口实现，每个任务可以独立设置超时时间和是否启用超时检测。需要注意的是，启用单任务超时检测会增加资源消耗，因为需要在监控线程中维护每个任务的计时器。在超时发生时，任务会被标记为 TIMEOUT 状态，defaultValue 方法的返回值作为降级结果传递给后续任务。这种双重超时机制为系统提供了可靠的时间保护，避免单个任务的长时间阻塞影响整体可用性。

### 组回调机制

IGroupCallback 接口定义了整组任务完成后的回调规范，与单任务回调 ICallback 不同，组回调关注的是整个任务图的整体执行结果。success 方法在所有任务成功完成时调用，failure 方法在超时或发生异常时调用。通过 IGroupCallback，调用方可以获取所有 WorkerWrapper 的列表，从中提取各任务的执行结果进行汇总分析。DefaultGroupCallback 提供了空实现作为默认回调。组回调常用于异步执行场景，通过 beginWorkAsync 方法触发异步执行并注册组回调，执行完成后自动触发相应回调。需要注意的是，框架不推荐大量使用组回调，因为它会在每次组执行时都触发一次回调，对于频繁执行的任务图可能会产生较多的回调开销。

```java
// 组回调接口
public interface IGroupCallback {
    void success(List<WorkerWrapper> workerWrappers);
    void failure(List<WorkerWrapper> workerWrappers, Exception e);
}

// 使用示例
IGroupCallback groupCallback = new IGroupCallback() {
    @Override
    public void success(List<WorkerWrapper> workerWrappers) {
        System.out.println("全部任务执行成功");
    }
    
    @Override
    public void failure(List<WorkerWrapper> workerWrappers, Exception e) {
        System.out.println("任务执行失败: " + e.getMessage());
    }
};

Async.beginWorkAsync(5000, groupCallback, wrapper1, wrapper2);
```

### 线程池管理

asyncTool 采用统一的线程池管理策略，通过 COMMON_POOL 和 executorService 两个静态字段维护线程池资源。COMMON_POOL 是基于 Executors.newCachedThreadPool() 创建的不定长线程池，具有自动回收空闲线程的能力，适合处理大量短生命周期任务。executorService 用于保存用户自定义的线程池引用，支持通过 beginWork 方法的参数注入。当使用默认线程池时，框架会自动管理 COMMON_POOL；当传入自定义线程池时，框架会优先使用自定义池并在执行结束后关闭。shutDown 方法提供了优雅关闭线程池的能力，会等待所有已提交任务执行完成后才关闭。getThreadCount 方法返回当前线程池的活跃线程数、已完成任务数、最大线程数等统计信息，可用于监控和调试。需要特别注意的是，由于 executorService 是静态字段，整个 JVM 生命周期中只能有一个自定义线程池生效。

```java
// 默认线程池配置
private static final ThreadPoolExecutor COMMON_POOL = (ThreadPoolExecutor) Executors.newCachedThreadPool();

// 使用默认线程池
Async.beginWork(5000, wrapper1, wrapper2);

// 使用自定义线程池
ExecutorService customPool = Executors.newFixedThreadPool(10);
Async.beginWork(5000, customPool, wrapper1, wrapper2);

// 关闭线程池
Async.shutDown(customPool);

// 查看线程池状态
String status = Async.getThreadCount();
```

### 状态机管理

WorkerWrapper 内部实现了完整的状态机机制，确保任务在并发环境下的安全执行。状态机定义了四种状态：INIT（0）表示初始化状态，WORKING（3）表示正在执行中，FINISH（1）表示执行成功完成，ERROR（2）表示执行失败或异常。状态的变更使用 AtomicInteger 的 compareAndSet 方法进行原子操作，确保并发修改的正确性。当任务状态已经是 FINISH 或 ERROR 时，后续的依赖触发会直接跳过，避免重复执行。状态机还与超时控制机制联动，stopNow 方法会将状态为 INIT 或 WORKING 的任务快速置为失败状态。这种设计使得框架能够正确处理复杂的并发依赖场景，即使多个前置任务几乎同时完成，也能保证后继任务只执行一次。

```java
// 状态常量定义
private static final int FINISH = 1;
private static final int ERROR = 2;
private static final int WORKING = 3;
private static final int INIT = 0;

// 原子状态管理
private AtomicInteger state = new AtomicInteger(0);

// 状态检查与更新
private void setState(int state) {
    this.state.set(state);
}

private int getState() {
    return this.state.get();
}
```

## 关键技术栈

### 基础运行环境

asyncTool 项目基于 Java 1.8 开发，充分利用了现代 Java 虚拟机的性能特性和语言特性。项目使用 Maven 3.3 作为构建工具，通过 pom.xml 文件进行依赖管理和项目配置。Java 1.8 的选择使得框架可以使用 Stream API、Lambda 表达式、CompletableFuture 等现代语言特性，这些特性在异步编程场景中尤为重要。Maven 编译插件配置指定 source 和 target 为 1.8，确保生成的字节码与目标运行环境兼容。项目采用标准的 Maven 多模块结构，源码位于 src/main/java 目录，测试代码位于 src/test/java 目录，配置文件位于 src/main/resources 目录。这种标准的项目结构使得 asyncTool 可以方便地集成到任何使用 Maven 的工程中，作为依赖库被其他模块引用。

### 并发编程框架

CompletableFuture 是 asyncTool 实现异步任务编排的核心技术基础，它是从 Java 1.8 引入的 Future 增强实现。框架使用 CompletableFuture.runAsync 方法将每个 WorkerWrapper 的执行包装为异步任务，使用 CompletableFuture.allOf 方法等待所有任务完成。CompletableFuture 的 get(timeout, TimeUnit) 方法提供了阻塞超时控制能力，是框架实现组级别超时的关键。与传统的 Thread 和 Runnable 相比，CompletableFuture 提供了更丰富的组合操作能力，支持链式调用、异常处理、超时控制等高级特性。同时，框架使用 ThreadPoolExecutor 管理实际的工作线程，通过自定义线程池参数可以灵活调整并发度和资源消耗。ScheduledExecutorService 用于 SystemClock 的定时时钟更新，确保高并发场景下时间获取的性能。

### 设计模式应用

asyncTool 框架大量使用了经典的设计模式来确保代码的灵活性和可扩展性。Builder 模式用于 WorkerWrapper 的构建，通过链式 API 提供清晰直观的配置接口，同时保持构造过程的安全性和灵活性。策略模式通过 IWorker、ICallback 等接口体现，不同的业务实现可以替换策略而不影响框架核心逻辑。模板方法模式在 WorkerWrapper 的 work 方法中有所体现，执行流程的框架已经固定，具体逻辑由回调方法提供。观察者模式通过 ICallback 回调机制实现，任务执行状态的变更会通知注册的观察者。适配器模式在扩展接口（如 ITimeoutWorker 扩展 IWorker）中得到应用，通过继承和默认方法实现接口的平滑扩展。这些设计模式的应用使得框架既保持了核心逻辑的稳定，又为扩展提供了良好的开放性。

### 依赖管理工具

项目当前版本（1.4.1-SNAPSHOT）未引入额外的第三方依赖，保持了极简的依赖结构。这种设计选择有多方面考虑：首先，减少依赖意味着更小的运行时包体积和更低的冲突风险；其次，核心功能完全基于 JDK 原生 API 实现，确保了框架的独立性和可移植性；再者，对于作为基础库使用的组件，最小化依赖可以降低对宿主项目的影响。在测试场景中，项目通过 src/test/java 目录组织测试代码，虽然 pom.xml 中未显式声明测试依赖，但可以通过添加 JUnit、TestNG 等测试框架进行单元测试和集成测试。这种轻量级的依赖策略使得 asyncTool 可以方便地集成到任何使用 Maven 的 Java 项目中，作为内部工具库使用。

### 高精度时钟

SystemClock 是框架提供的一个高并发场景下的时间获取工具类，用于替代 System.currentTimeMillis()。在高并发环境中，System.currentTimeMillis() 可能因为系统调用而产生性能瓶颈，SystemClock 通过ScheduledExecutorService 定时更新原子变量中的时间戳，使得后续的时间读取操作只需读取内存变量而无需系统调用。实现上使用单例模式，通过静态内部类 InstanceHolder 确保线程安全的延迟初始化。时钟更新周期默认为 1 毫秒，通过 scheduleAtFixedRate 方法定期更新 now 原子变量。这种设计在牺牲一定时间精度的前提下大幅提升了高并发场景下的时间获取性能，对于异步任务超时控制等场景完全足够使用。

```java
// 高精度时钟实现
public class SystemClock {
    private final int period;
    private final AtomicLong now;
    
    private static class InstanceHolder {
        private static final SystemClock INSTANCE = new SystemClock(1);
    }
    
    public static long now() {
        return instance().currentTimeMillis();
    }
}
```

### 异常处理机制

框架定义了 SkippedException 作为任务跳过异常，当任务在执行前检测到其后续任务已经执行完成或正在执行时，会抛出该异常并跳过当前任务的执行。这种设计主要用于处理依赖链路的剪枝场景，避免在复杂的依赖图中执行冗余的计算。SkippedException 继承自 RuntimeException，无需在方法签名中声明，符合异步编程中异常处理的惯例。WorkResult 类中的 ex 字段用于记录任务执行过程中的异常信息，通过 ResultState.EXCEPTION 状态标识异常发生。框架在 IWorker.action 方法的执行过程中会捕获所有异常并封装到 WorkResult 中，确保异常不会传播到框架核心层导致任务执行中断。这种设计使得异步任务的异常可以被正确捕获和处理，而不是简单地抛出到调用方。

### 执行流程图

```
任务提交阶段
    |
    v
+------------------+
| Async.beginWork  |  接收 WorkerWrapper 列表
+------------------+
    |
    v
+------------------+
| CompletableFuture |  为每个 Wrapper 创建异步任务
|    .runAsync()   |
+------------------+
    |
    v
+------------------+
|  WorkerWrapper   |  执行实际的任务逻辑
|     .work()      |
+------------------+
    |              |
    |              v
    |     +------------------+
    |     |  依赖检查       |  检查前置依赖是否满足
    |     +------------------+
    |              |
    |     +------------------+
    |     |  IWorker.action |  执行具体业务逻辑
    |     +------------------+
    |              |
    |     +------------------+
    |     | ICallback.result |  回调结果
    |     +------------------+
    |              |
    v              v
+------------------+
|  超时检测       |  检测整体是否超时
+------------------+
    |
    v
+------------------+
|   stopNow()      |  超时则停止所有任务
+------------------+
```

## 使用指南

### 快速开始

使用 asyncTool 框架的基本流程包括：定义执行单元实现 IWorker 接口、定义回调实现 ICallback 接口、使用 Builder 创建 WorkerWrapper、调用 Async.beginWork 执行任务。以下是一个完整的示例，展示了如何并行执行两个独立任务并等待结果：

```java
// 定义执行单元
IWorker<String, Integer> worker1 = (param, allWrappers) -> {
    System.out.println("Worker1 开始处理: " + param);
    Thread.sleep(1000);
    return param.length();
};

IWorker<String, Integer> worker2 = (param, allWrappers) -> {
    System.out.println("Worker2 开始处理: " + param);
    Thread.sleep(1500);
    return param.hashCode();
};

// 定义回调
ICallback<String, Integer> callback = (success, param, result) -> {
    System.out.println("任务结果 - 成功: " + success + ", 结果: " + result.getResult());
};

// 创建包装器
WorkerWrapper<String, Integer> wrapper1 = new WorkerWrapper.Builder<String, Integer>()
    .worker(worker1)
    .param("hello")
    .callback(callback)
    .build();

WorkerWrapper<String, Integer> wrapper2 = new WorkerWrapper.Builder<String, Integer>()
    .worker(worker2)
    .param("hello")
    .callback(callback)
    .build();

// 执行任务
boolean completed = Async.beginWork(5000, wrapper1, wrapper2);
System.out.println("任务是否在超时前完成: " + completed);

// 关闭线程池
Async.shutDown();
```

### 串行执行示例

通过 depend 和 next 方法可以构建串行的任务执行链，每个任务在前一个任务完成后才开始执行：

```java
// 创建三个任务
WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
    .worker((param, wrappers) -> "步骤1完成")
    .build();

WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
    .worker((param, wrappers) -> "步骤2完成")
    .build();

WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
    .worker((param, wrappers) -> "步骤3完成")
    .build();

// 设置依赖关系：wrapper2 依赖 wrapper1，wrapper3 依赖 wrapper2
wrapper2.depend(wrapper1);
wrapper3.depend(wrapper2);

// 执行串行任务
Async.beginWork(10000, wrapper1, wrapper2, wrapper3);
```

### 并行执行示例

多个任务没有依赖关系时会并行执行，框架会自动调度：

```java
// 创建四个独立任务
WorkerWrapper<String, Integer> task1 = new WorkerWrapper.Builder<String, Integer>()
    .worker(new TaskWorker("任务1"))
    .param("data1")
    .build();

WorkerWrapper<String, Integer> task2 = new WorkerWrapper.Builder<String, Integer>()
    .worker(new TaskWorker("任务2"))
    .param("data2")
    .build();

WorkerWrapper<String, Integer> task3 = new WorkerWrapper.Builder<String, Integer>()
    .worker(new TaskWorker("任务3"))
    .param("data3")
    .build();

WorkerWrapper<String, Integer> task4 = new WorkerWrapper.Builder<String, Integer>()
    .worker(new TaskWorker("任务4"))
    .param("data4")
    .build();

// 无依赖关系，四个任务并行执行
Async.beginWork(5000, task1, task2, task3, task4);
```

### 混合依赖示例

复杂的业务场景通常需要混合使用串行和并行依赖：

```java
// 任务结构：
//     task1 -----+---> task3
//     task2 -----+---> task4 ---> task6
//     task5 -----+

WorkerWrapper<String, String> task1 = new WorkerWrapper.Builder<String, String>()
    .worker(w1).build();
WorkerWrapper<String, String> task2 = new WorkerWrapper.Builder<String, String>()
    .worker(w2).build();
WorkerWrapper<String, String> task3 = new WorkerWrapper.Builder<String, String>()
    .worker(w3).build();
WorkerWrapper<String, String> task4 = new WorkerWrapper.Builder<String, String>()
    .worker(w4).build();
WorkerWrapper<String, String> task5 = new WorkerWrapper.Builder<String, String>()
    .worker(w5).build();
WorkerWrapper<String, String> task6 = new WorkerWrapper.Builder<String, String>()
    .worker(w6).build();

// 设置依赖
task3.depend(task1, task2);
task4.depend(task1, task2);
task6.depend(task3, task4, task5);

// 执行
Async.beginWork(10000, task1, task2, task3, task4, task5, task6);
```

## 总结

asyncTool 是一个轻量级但功能强大的 Java 异步任务编排框架，通过简洁的 API 设计和完善的机制支持，为开发者提供了便捷的异步编程能力。框架的核心设计理念是将复杂的并发控制、依赖管理、超时处理等逻辑封装在内部，让开发者能够专注于业务逻辑本身。从技术实现角度看，框架综合运用了 CompletableFuture、线程池、原子变量、状态机等并发编程技术，在保证功能完整性的同时保持了代码的简洁性。WorkerWrapper 作为核心的包装组件，通过 Builder 模式提供了灵活的配置能力，支持串行、并行、混合等多种执行模式。依赖管理机制通过 DependWrapper 实现了 AND 和 OR 两种依赖模式，满足了复杂业务流程的需求。双重超时控制机制为系统提供了可靠的时间保护，避免单个任务的阻塞影响整体可用性。回调机制实现了执行逻辑与结果处理的分离，提升了代码的可维护性。轻量级的依赖策略使得框架可以方便地集成到任何 Java 项目中，是构建高性能异步系统的理想选择。