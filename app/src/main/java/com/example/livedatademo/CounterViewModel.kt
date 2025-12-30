package com.example.livedatademo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * CounterViewModel - 计数器功能的 ViewModel
 * 
 * ViewModel 的作用：
 * 1. 管理 UI 相关的数据（与 UI 生命周期无关）
 * 2. 在配置变更（如屏幕旋转）时保留数据
 * 3. 处理业务逻辑，与 UI 分离
 * 
 * LiveData 架构模式：
 * - 使用 MutableLiveData 作为内部可变数据源（_counter, _message）
 * - 对外暴露不可变的 LiveData（counter, message）
 * - 这是封装的最佳实践：防止外部直接修改数据
 * 
 * 命名约定：
 * - 私有变量使用下划线前缀：_counter（MutableLiveData，可写）
 * - 公开变量不使用下划线：counter（LiveData，只读）
 */
class CounterViewModel : ViewModel() {

    /**
     * MutableLiveData - 可变的 LiveData
     * 
     * 关键特性：
     * 1. 生命周期感知：只有活跃的观察者才会收到更新
     * 2. 主线程安全：value 必须在主线程设置
     * 3. 线程安全：postValue() 可以在任何线程调用
     * 
     * apply {} 作用域函数：
     * - 在对象创建后立即执行代码块
     * - 返回对象本身，可以链式调用
     * - 这里用于设置初始值
     */
    private val _counter = MutableLiveData<Int>().apply {
        value = 0 // 设置初始值
    }
    
    /**
     * LiveData - 不可变的 LiveData（只读）
     * 
     * 封装模式：
     * - 外部只能观察（observe），不能修改
     * - 只有 ViewModel 内部可以修改 _counter
     * - 保证数据修改的唯一入口，便于管理和调试
     */
    val counter: LiveData<Int> = _counter

    /**
     * 消息 LiveData
     * - 用于显示操作提示信息
     * - 初始值为空字符串
     */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    /**
     * 增加计数
     * 
     * value 属性说明：
     * - 必须在主线程调用
     * - 同步更新，立即通知所有观察者
     * - _counter.value?:0 使用 Elvis 操作符，如果为 null 则使用 0
     */
    fun increment() {
        _counter.value = (_counter.value?:0) + 1
        _message.value = "手动增加了计数"
    }

    /**
     * 减少计数
     * - 在主线程中同步更新
     */
    fun decrement() {
        _counter.value = (_counter.value?:0) -1
        _message.value = "手动减少了计数"
    }

    /**
     * 重置计数
     * - 将计数器重置为 0
     */
    fun reset() {
        _counter.value = 0
        _message.value = "计数已重置"
    }

    /**
     * 模拟网络请求
     * 
     * viewModelScope 说明：
     * - ViewModel 提供的协程作用域
     * - 当 ViewModel 被清除时，自动取消所有协程
     * - 避免内存泄漏：Activity/Fragment 销毁时，协程自动取消
     * 
     * 执行流程：
     * 1. 立即更新 message（显示加载状态）
     * 2. 在协程中延迟 2 秒（模拟网络请求）
     * 3. 更新 counter 和 message（显示结果）
     * 
     * 注意：
     * - 在协程中，虽然 delay() 会挂起，但后续的 value 赋值仍在主线程
     * - 因为 viewModelScope 默认使用 Dispatchers.Main
     */
    fun fetchData() {
        _message.value = "正在加载数据..."
        viewModelScope.launch {
            delay(2000) // 延迟 2 秒，模拟网络请求

            // 在协程中更新 LiveData（仍在主线程）
            _counter.value = 100
            _message.value = "数据已经加载完成！ 获得新计数： 100"
        }
    }

    /**
     * 从后台线程更新数据
     * 
     * postValue() vs value 的区别：
     * 
     * value（主线程）：
     * - 必须在主线程调用
     * - 同步更新，立即通知观察者
     * - 如果不在主线程调用会抛出异常
     * 
     * postValue()（任何线程）：
     * - 可以在任何线程调用（包括后台线程）
     * - 异步更新，将值发布到主线程
     * - 线程安全，适合在协程或后台线程使用
     * - 注意：如果快速连续调用，可能只保留最后一个值
     * 
     * 使用场景：
     * - 网络请求回调（在后台线程）
     * - 数据库查询结果（在 IO 线程）
     * - 任何需要在非主线程更新 UI 数据的场景
     */
    fun updateFromBackground() {
        viewModelScope.launch {
            delay(1000) // 延迟 1 秒
            
            // 使用 postValue() 在后台线程更新（虽然这里仍在主线程，但演示用法）
            // 如果在 withContext(Dispatchers.IO) 中，就必须用 postValue()
            _counter.postValue(50)
            _message.postValue("从后台线程更新")
        }
    }
}