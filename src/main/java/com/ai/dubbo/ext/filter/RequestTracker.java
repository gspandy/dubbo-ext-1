package com.ai.dubbo.ext.filter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.dubbo.ext.vo.BaseResponse;
import com.ai.paas.ipaas.PaaSConstant;
import com.ai.paas.ipaas.util.DateTimeUtil;
import com.ai.paas.ipaas.util.UUIDTool;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import com.google.gson.Gson;

@Activate(group = { Constants.PROVIDER })
public class RequestTracker implements Filter {

	private static final Logger LOG = LoggerFactory
			.getLogger(RequestTracker.class);

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) {
		// 获取访问协议
		String protocol = invoker.getUrl().getProtocol();
		String reqSV = invoker.getInterface().getName();
		String reqMethod = invocation.getMethodName();
		Object[] reqParams = invocation.getArguments();
		// 交易序列号+时间
		String reqSeq = DateTimeUtil.dateToString(new java.util.Date(),
				"yyyyMMddHHmmss") + "_" + UUIDTool.genId32();
		Gson gson = new Gson();
		// 获取返回类型
		Class retType = getReturnType(invoker, reqMethod, reqParams);
		// 打印请求参数明细
		if (null != reqParams && reqParams.length >= 0) {
			if (LOG.isInfoEnabled()) {
				LOG.info(
						"req_seq:{},req_protocol:{},srv_name:{},srv_method:{},srv_params:{}",
						reqSeq, protocol, reqSV, reqMethod,
						gson.toJson(reqParams));
			}
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info(
						"req_seq:{},req_protocol:{},srv_name:{},srv_method:{},srv_params:{}",
						reqSeq, protocol, reqSV, reqMethod, "blank");
			}
		}
		// 执行结果
		Result result = null;
		try {
			result = invoker.invoke(invocation);
			if (LOG.isInfoEnabled()) {
				LOG.info(
						"req_seq:{} call over....!,req_protocol:{},srv_name:{},srv_method:{},result{}",
						reqSeq, protocol, reqSV, reqMethod, result);
			}
			return writeCallResult(protocol, result, retType);
		} catch (Throwable ex) {
			// 此处保留不动
			if (LOG.isErrorEnabled()) {
				LOG.error("req_seq:{},执行{}类中的{}方法发生异常:{}", reqSeq, reqSV,
						reqMethod, ex);
			}
			return writeCallResult(protocol, result, retType);
		} finally {
			if (null == gson) {
				gson = null;
			}
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Result writeCallResult(String protocol, Result result, Class retType) {
		if (null != protocol && protocol.equalsIgnoreCase("rest")
				&& null == result.getValue() && result.hasException()
				&& null != result.getException()) {

			Throwable ex = result.getException();
			// 转换成正常的结果返回
			BaseResponse response = new BaseResponse(false,
					PaaSConstant.ExceptionCode.SYSTEM_ERROR, ex.getMessage(),
					ex.getStackTrace());

			if (null != retType && BaseResponse.class.isAssignableFrom(retType)) {
				RpcResult r = new RpcResult();
				Gson gson = new Gson();
				r.setValue(gson.fromJson(gson.toJson(response), retType));
				return r;
			} else {
				// 可能什么也没返回，或者不是这种类型
				return result;
			}

		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	private Class getReturnType(Invoker<?> invoker, String reqMethod,
			Object[] reqParams) {

		List<Class> paramTypes = new ArrayList<>();
		if (null != reqParams) {
			for (Object param : reqParams) {
				paramTypes.add(param.getClass());
			}
		}
		try {
			Method target = invoker.getInterface().getMethod(reqMethod,
					paramTypes.toArray(new Class[paramTypes.size()]));
			if (null == target)
				return null;
			Class returnType = target.getReturnType();
			return returnType;
		} catch (Exception e) {
			LOG.error("reqMethod:{},paramTypes{},invocation{}方法发生异常:{}",
					reqMethod, reqParams, invoker.getInterface(), e);
			return null;
		}
	}
}