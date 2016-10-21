package com.ai.dubbo.ext.vo;

import java.io.Serializable;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.context.i18n.LocaleContextHolder;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public BaseInfo() {
		//set in the web
		if (null != LocaleContextHolder.getLocale()) {
			locale = LocaleContextHolder.getLocale();
		}
	}

	/**
	 * 租户Id，必填
	 */
	private String tenantId;
	/**
	 * 租户密码，可选
	 */
	private String tenantPwd;
	/**
	 * 区域属性
	 */
	private Locale locale;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantPwd() {
		return tenantPwd;
	}

	public void setTenantPwd(String tenantPwd) {
		this.tenantPwd = tenantPwd;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}
