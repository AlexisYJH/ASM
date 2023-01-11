# AMS
字节码插桩就是在java字节码文件中的某些位置插入或修改代码。

ASM：操作java字节码框架，按照Class文件的格式，解析、修改、生成Class，可以动态生成类或者增强既有类的功能。

## AMS的使用
1. 引入ASM依赖
   testImplementation 'org.ow2.asm:asm:9.4'
   testImplementation 'org.ow2.asm:asm-commons:9.4'    
 
2. 准备待插桩Class：javac

3. 编码实现
- ClassReader(accept), ClassVisitor(visitMethod, visitAnnotation), MethodVisitor子类AdviceAdapter(onMethodEnter, onMethodExit), ClassWriter(toByteArray)
- 栈帧
- 字节码指令
    命令：javap -c xxx.class
    插件：ASM ByteCode Viewer
- 方法签名
    boolean Z
    byte B
    char C
    String Ljava/lang/String;
- 只在特定方法中插桩：注解

插桩与APT的区别：
- 插桩基于class，修改生成class；无侵入性；
- APT基于Java，生成java；生成一个新的java类；使用有侵入性；

## Android实现
1. 如何获取所有的Class？ 
   PMS+DexFile

2. Android打包流程
   插桩时机：编译后打包前

3. 其他场景：热修复
   CLASS_ISPREVERIFIED标志：在Dalvik虚拟机中，如果一个类所调用的Class全部在一个Dex文件中，则就会被打上CLASS_ISPREVERIFIED标志；
   利用字节码插桩技术：可在每个类中添加一个特殊类的调用，这个类单独编译生成一个dex文件，此时在加载的时候就不会被打上CLASS_ISPREVERIFIED标志，进而就能校验通过，进行替换修复；