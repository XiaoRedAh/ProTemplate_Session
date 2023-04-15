import axios from "axios";
import {ElMessage} from "element-plus";

//失败和异常给个默认处理就行
const defaultError = ()=>ElMessage.error('发生了一些错误，请联系管理员')
const defaultFailure = (message)=>ElMessage.warning(message)

//封装axios的post方法
//data就是接收后端响应过来的数据。后端那边统一了响应类，也就是RestBean
function post(url, data, success, failure = defaultFailure, error = defaultError){
    axios.post(url, data, {//配置
        //由于都是以表单形式提交，因此设置headers的Content-Type为表单形式
        headers:{
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        withCredentials: true//发起请求，携带cookie（基于session的校验需要cookie，以后用jwt的时候就不用cookie了）
    }).then(({data})=>{
        if(data.success)
            success(data.message, data.status)
        else
            failure(data.message, data.status)
    }).catch(error)
}

//封装axios的get方法（get没有data）
function get(url, success, failure = defaultFailure, error = defaultError){
    axios.get(url,{
        //因为get方法不需要提交东西，所以不用配置headers
        withCredentials: true//发起请求，携带cookie（基于session的校验需要cookie，以后用jwt的时候就不用cookie了）
    }).then(({data})=>{
        if(data.success)
            success(data.message, data.status,)
        else
            failure(data.message, data.status)
    }).catch(error)
}

//封装好后，暴露出去
export {get, post}