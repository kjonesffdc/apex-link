package com.sforce.soap.tooling;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a generated class for the SObject Enterprise API.
 * Do not edit this file, as your changes will be lost.
 */
public enum SharingModel {


  
	/**
	 * Enumeration  : Private
	 */
	Private("Private"),

  
	/**
	 * Enumeration  : Read
	 */
	Read("Read"),

  
	/**
	 * Enumeration  : ReadSelect
	 */
	ReadSelect("ReadSelect"),

  
	/**
	 * Enumeration  : ReadWrite
	 */
	ReadWrite("ReadWrite"),

  
	/**
	 * Enumeration  : ReadWriteTransfer
	 */
	ReadWriteTransfer("ReadWriteTransfer"),

  
	/**
	 * Enumeration  : FullAccess
	 */
	FullAccess("FullAccess"),

  
	/**
	 * Enumeration  : ControlledByParent
	 */
	ControlledByParent("ControlledByParent"),

  
	/**
	 * Enumeration  : ControlledByLeadOrContact
	 */
	ControlledByLeadOrContact("ControlledByLeadOrContact"),

  
	/**
	 * Enumeration  : ControlledByCampaign
	 */
	ControlledByCampaign("ControlledByCampaign"),

;

	public static Map<String, String> valuesToEnums;

	static {
   		valuesToEnums = new HashMap<String, String>();
   		for (SharingModel e : EnumSet.allOf(SharingModel.class)) {
   			valuesToEnums.put(e.toString(), e.name());
   		}
   	}

   	private String value;

   	private SharingModel(String value) {
   		this.value = value;
   	}

   	@Override
   	public String toString() {
   		return value;
   	}
}