package com.example.livedatademo

import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.livedatademo.ui.theme.LiveDataDemoTheme

/**
 * MainActivity - 应用程序的主入口 Activity
 * 
 * 架构说明：
 * 1. 使用 Jetpack Compose 构建 UI（声明式 UI 框架）
 * 2. 使用 ViewModel + LiveData 架构模式管理 UI 相关的数据
 * 3. 遵循 MVVM 架构：View(Compose UI) <-> ViewModel <-> Model(数据源)
 * 
 * 数据流向：
 * ViewModel (LiveData) -> observeAsState() -> Compose State -> UI 自动重组
 * 用户操作 -> ViewModel 方法 -> 更新 LiveData -> UI 自动更新
 */
class MainActivity : ComponentActivity() {

    /**
     * ViewModel 的获取方式：使用委托属性 by viewModels()
     * 
     * 关键点：
     * - viewModels() 是 Activity 的扩展函数，用于创建或获取 ViewModel 实例
     * - 使用委托属性（by）实现懒加载，首次访问时才创建 ViewModel
     * - ViewModel 的生命周期与 Activity 绑定，但配置变更（如屏幕旋转）时不会重建
     * - 这是 ViewModel 的核心优势：数据在配置变更时得以保留
     */
    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        /**
         * enableEdgeToEdge() - 启用边缘到边缘显示
         * 
         * 作用：
         * - 让应用内容延伸到状态栏和导航栏下方
         * - 实现沉浸式体验，内容可以显示在系统栏下方
         * - 需要配合 Scaffold 的 innerPadding 来避免内容被系统栏遮挡
         */
        enableEdgeToEdge()
        
        /**
         * setContent {} - Compose 的入口点
         * 
         * 作用：
         * - 将 Compose UI 设置到 Activity 的根视图
         * - 这是从传统 View 系统切换到 Compose 的关键函数
         * - 内部使用 @Composable 函数构建 UI
         */
        setContent {
            // LiveDataDemoTheme - 应用主题，提供 Material Design 3 的颜色、字体等样式
            LiveDataDemoTheme {
                /**
                 * Scaffold - Material Design 的布局容器
                 * 
                 * 关键参数：
                 * - modifier: fillMaxSize() 填充整个屏幕（宽度和高度）
                 * - content: lambda 接收 innerPadding 参数
                 * 
                 * innerPadding 的作用：
                 * - 自动计算系统窗口 insets（状态栏、导航栏的高度）
                 * - 确保内容不会被系统栏遮挡
                 * - 必须传递给子组件才能生效
                 */
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 将 innerPadding 传递给 CounterScreen，确保内容不被系统栏遮挡
                    CounterScreen(viewModel, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * CounterScreen - 计数器屏幕的 Compose UI
 * 
 * @Composable 注解：
 * - 标记这是一个可组合函数（Composable Function）
 * - Compose 编译器会将其转换为特殊的 UI 树结构
 * - 函数内部可以调用其他 @Composable 函数来构建 UI
 * 
 * 参数说明：
 * @param viewModel ViewModel 实例，包含业务逻辑和数据
 * @param modifier Modifier 链，用于设置布局属性（padding、size 等）
 *                 注意：这里传入的 modifier 包含 innerPadding，用于处理系统栏间距
 */
@Composable
fun CounterScreen(viewModel: CounterViewModel, modifier: Modifier) {

    /**
     * observeAsState() - LiveData 转换为 Compose State 的关键函数
     * 
     * 工作原理：
     * 1. 观察 ViewModel 中的 LiveData（counter）
     * 2. 当 LiveData 的值发生变化时，自动更新 Compose State
     * 3. State 变化触发 Compose 重组（Recomposition），UI 自动更新
     * 
     * 语法说明：
     * - val counter by ... 使用委托属性（by），自动调用 getValue()
     * - 参数 0 是初始值，在 LiveData 还没有值时使用
     * 
     * 生命周期感知：
     * - 当 Composable 进入组合时开始观察
     * - 当 Composable 离开组合时自动取消观察
     * - 这是 LiveData 生命周期感知的核心体现
     */
    val counter by viewModel.counter.observeAsState(0)
    val message by viewModel.message.observeAsState("")

    /**
     * Column - 垂直布局容器
     * 
     * Modifier 链式调用说明：
     * - modifier: 使用传入的 modifier（包含 innerPadding，处理系统栏间距）
     * - fillMaxSize(): 填充父容器的全部宽度和高度
     * - padding(16.dp): 添加 16dp 的内边距
     * 
     * 注意：Modifier 的顺序很重要！
     * 先 fillMaxSize() 再 padding()，表示：先填充全部空间，再添加内边距
     * 如果顺序相反，效果会不同
     * 
     * 布局参数：
     * - horizontalAlignment: 水平方向对齐方式（居中）
     * - verticalArrangement: 垂直方向排列方式（spacedBy 表示子元素之间固定间距）
     */
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题文本
        Text(text = "LiveData演示",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
             modifier = Modifier.padding(bottom = 8.dp))

        /**
         * Card - Material Design 3 的卡片组件
         * 
         * 关键点：
         * - fillMaxWidth(): 填充父容器的全部宽度
         * - colors: 设置卡片颜色，使用 MaterialTheme 的主题色
         * - 内部可以放置任何 Composable 内容
         */
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "当前计数：",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp))
                
                /**
                 * 显示计数器数值
                 * 
                 * counter.toString() - 将 Int 转换为 String
                 * 
                 * 关键点：
                 * - counter 是 Compose State，来自 observeAsState()
                 * - 当 ViewModel 中的 LiveData 更新时，counter 自动更新
                 * - Compose 检测到 counter 变化，自动重组这个 Text，显示新值
                 * - 这就是响应式 UI 的核心：数据驱动 UI 更新
                 */
                Text(text = counter.toString(),
                    fontSize = 64.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        /**
         * 条件渲染：只在 message 不为空时显示
         * 
         * Compose 的条件渲染：
         * - 使用 if 语句控制是否显示组件
         * - 当条件变化时，Compose 会自动添加或移除组件
         * - 这是声明式 UI 的特点：描述"应该显示什么"，而不是"如何显示"
         */
        if (message.isNotEmpty()) {
            Text(text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp))
        }

        // Spacer - 空白间距组件，用于在布局中创建固定高度的空白
        Spacer(modifier = Modifier.height(8.dp))

        // 按钮区域容器
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        /**
         * Row - 水平布局容器
         * 
         * 布局说明：
         * - 子元素水平排列
         * - horizontalArrangement.spacedBy(12.dp): 子元素之间固定 12dp 间距
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /**
             * OutlinedButton - 带边框的按钮（次要操作）
             * 
             * onClick 回调：
             * - 点击时调用 ViewModel 的 decrement() 方法
             * - ViewModel 更新 LiveData
             * - LiveData 变化触发 observeAsState() 更新 State
             * - State 变化触发 Compose 重组，UI 自动更新
             * 
             * modifier.weight(1f):
             * - 在 Row 中，weight 让按钮平均分配剩余空间
             * - 两个按钮各占 50% 宽度
             */
            OutlinedButton(
                onClick = {
                    // 调用 ViewModel 方法，更新数据
                    viewModel.decrement()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "减少")
            }

            /**
             * Button - 实心按钮（主要操作）
             * 
             * 与 OutlinedButton 的区别：
             * - Button: 实心背景，用于主要操作
             * - OutlinedButton: 只有边框，用于次要操作
             */
            Button(
                onClick = {viewModel.increment()},
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "增加")
            }
        }

        /**
         * 重置按钮
         * - fillMaxWidth(): 填充全部宽度（与上面的两个按钮不同）
         */
        OutlinedButton(
            onClick = {viewModel.reset()},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "重置为0")
        }

        /**
         * 模拟网络请求按钮
         * 
         * 演示场景：
         * - 点击后，ViewModel 在协程中延迟 2 秒
         * - 然后更新 LiveData
         * - 演示 LiveData 在异步操作中的使用
         * 
         * colors 参数：
         * - 自定义按钮颜色，使用主题的 secondary 颜色
         */
        Button(
            onClick = {viewModel.fetchData()},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "模拟网络请求(2S)")
        }

        /**
         * 后台线程更新按钮
         * 
         * 演示场景：
         * - 演示在后台线程中使用 postValue() 更新 LiveData
         * - postValue() 是线程安全的，可以在任何线程调用
         * - 与 value 的区别：value 只能在主线程调用
         */
        Button(
            onClick = {viewModel.updateFromBackground()},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "后台更新线程(1s)")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("LiveData特点演示",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "• 主线程更新: 点击 +/- 按钮\n" +
                            "• 异步更新: 点击模拟网络请求\n" +
                            "• 后台线程更新: 使用 postValue\n" +
                            "• 生命周期感知: 旋转屏幕观察",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
        }
    }
}

/**
 * @Preview - Compose 预览注解
 * 
 * 作用：
 * - 在 Android Studio 的预览窗口中显示这个 Composable
 * - 不需要运行应用就能看到 UI 效果
 * - 提高开发效率
 * 
 * 参数说明：
 * - showBackground: 显示背景
 * - showSystemUi: 显示系统 UI（状态栏、导航栏）
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CounterScreenPreview() {
    LiveDataDemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize()) {
                // 预览时创建一个新的 ViewModel 实例
                CounterScreen(viewModel = CounterViewModel(), modifier = Modifier.padding(10.dp))
            }
    }
}