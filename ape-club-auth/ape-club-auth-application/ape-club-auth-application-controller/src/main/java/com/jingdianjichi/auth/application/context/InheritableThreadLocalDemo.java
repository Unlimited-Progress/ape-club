package com.jingdianjichi.auth.application.context;

public class InheritableThreadLocalDemo {
    // 使用 InheritableThreadLocal 而非 ThreadLocal
    private static final InheritableThreadLocal<String> TRACE_ID = new InheritableThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {
        // 主线程设置值
        TRACE_ID.set("TRACE-123");
        System.out.println("Main thread: " + TRACE_ID.get()); // 输出: TRACE-123

        // 创建子线程
        Thread child = new Thread(() -> {
            System.out.println("Child thread: " + TRACE_ID.get()); // 输出: TRACE-123 (继承成功!)
            
            // 子线程修改值（不影响主线程）
            TRACE_ID.set("CHILD-TRACE");
            System.out.println("Child modified: " + TRACE_ID.get());
        });

        child.sleep(10000);
        child.start();
        child.join();

        // 主线程值未变
        System.out.println("Main after child: " + TRACE_ID.get()); // 仍为 TRACE-123
    }
}