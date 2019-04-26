package com.sforce.soap.tooling;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a generated class for the SObject Enterprise API.
 * Do not edit this file, as your changes will be lost.
 */
public enum Oauth2CannedScopes {


  
	/**
	 * Enumeration  : id
	 */
	id("id"),

  
	/**
	 * Enumeration  : api
	 */
	api("api"),

  
	/**
	 * Enumeration  : web
	 */
	web("web"),

  
	/**
	 * Enumeration  : full
	 */
	full("full"),

  
	/**
	 * Enumeration  : chatter_api
	 */
	chatter_api("chatter_api"),

  
	/**
	 * Enumeration  : visualforce
	 */
	visualforce("visualforce"),

  
	/**
	 * Enumeration  : refresh_token
	 */
	refresh_token("refresh_token"),

  
	/**
	 * Enumeration  : openid
	 */
	openid("openid"),

  
	/**
	 * Enumeration  : profile
	 */
	profile("profile"),

  
	/**
	 * Enumeration  : email
	 */
	email("email"),

  
	/**
	 * Enumeration  : address
	 */
	address("address"),

  
	/**
	 * Enumeration  : phone
	 */
	phone("phone"),

  
	/**
	 * Enumeration  : offline_access
	 */
	offline_access("offline_access"),

  
	/**
	 * Enumeration  : custom_permissions
	 */
	custom_permissions("custom_permissions"),

  
	/**
	 * Enumeration  : wave_api
	 */
	wave_api("wave_api"),

  
	/**
	 * Enumeration  : eclair_api
	 */
	eclair_api("eclair_api"),

;

	public static Map<String, String> valuesToEnums;

	static {
   		valuesToEnums = new HashMap<String, String>();
   		for (Oauth2CannedScopes e : EnumSet.allOf(Oauth2CannedScopes.class)) {
   			valuesToEnums.put(e.toString(), e.name());
   		}
   	}

   	private String value;

   	private Oauth2CannedScopes(String value) {
   		this.value = value;
   	}

   	@Override
   	public String toString() {
   		return value;
   	}
}