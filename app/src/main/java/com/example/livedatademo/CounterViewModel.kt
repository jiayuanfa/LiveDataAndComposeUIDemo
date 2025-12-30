package com.example.livedatademo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CounterViewModel : ViewModel() {

    private val _counter = MutableLiveData<Int>().apply {
        value = 0 // 设置初始值
    }
    val counter: LiveData<Int> = _counter

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun increment() {
        _counter.value = (_counter.value?:0) + 1
        _message.value = "手动增加了计数"
    }

    fun decrement() {
        _counter.value = (_counter.value?:0) -1
        _message.value = "手动减少了计数"
    }

    fun reset() {
        _counter.value = 0
        _message.value = "计数已重置"
    }

    fun fetchData() {
        _message.value = "正在加载数据..."
        viewModelScope.launch {
            delay(2000)

            _counter.value = 100
            _message.value = "数据已经加载完成！ 获得新计数： 100"
        }
    }

    fun updateFromBackground() {
        viewModelScope.launch {
            delay(1000)
            _counter.postValue(50)
            _message.postValue("从后台线程更新")
        }
    }
}