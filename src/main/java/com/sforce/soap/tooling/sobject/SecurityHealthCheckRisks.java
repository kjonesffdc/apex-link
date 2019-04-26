package com.sforce.soap.tooling.sobject;

/**
 * This is a generated class for the SObject Enterprise API.
 * Do not edit this file, as your changes will be lost.
 */
public class SecurityHealthCheckRisks extends com.sforce.soap.tooling.sobject.SObject {

    /**
     * Constructor
     */
    public SecurityHealthCheckRisks() {}

    /* Cache the typeInfo instead of declaring static fields throughout*/
    private transient java.util.Map<String, com.sforce.ws.bind.TypeInfo> typeInfoCache = new java.util.HashMap<String, com.sforce.ws.bind.TypeInfo>();
    private com.sforce.ws.bind.TypeInfo _lookupTypeInfo(String fieldName, String namespace, String name, String typeNS, String type, int minOcc, int maxOcc, boolean elementForm) {
      com.sforce.ws.bind.TypeInfo typeInfo = typeInfoCache.get(fieldName);
      if (typeInfo == null) {
        typeInfo = new com.sforce.ws.bind.TypeInfo(namespace, name, typeNS, type, minOcc, maxOcc, elementForm);
        typeInfoCache.put(fieldName, typeInfo);
      }
      return typeInfo;
    }

    /**
     * element : DurableId of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean DurableId__is_set = false;

    private java.lang.String DurableId;

    public java.lang.String getDurableId() {
      return DurableId;
    }

    public void setDurableId(java.lang.String DurableId) {
      this.DurableId = DurableId;
      DurableId__is_set = true;
    }

    protected void setDurableId(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("DurableId", "urn:sobject.tooling.soap.sforce.com","DurableId","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setDurableId(__typeMapper.readString(__in, _lookupTypeInfo("DurableId", "urn:sobject.tooling.soap.sforce.com","DurableId","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldDurableId(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("DurableId", "urn:sobject.tooling.soap.sforce.com","DurableId","http://www.w3.org/2001/XMLSchema","string",0,1,true), DurableId, DurableId__is_set);
    }

    /**
     * element : OrgValue of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean OrgValue__is_set = false;

    private java.lang.String OrgValue;

    public java.lang.String getOrgValue() {
      return OrgValue;
    }

    public void setOrgValue(java.lang.String OrgValue) {
      this.OrgValue = OrgValue;
      OrgValue__is_set = true;
    }

    protected void setOrgValue(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("OrgValue", "urn:sobject.tooling.soap.sforce.com","OrgValue","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setOrgValue(__typeMapper.readString(__in, _lookupTypeInfo("OrgValue", "urn:sobject.tooling.soap.sforce.com","OrgValue","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldOrgValue(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("OrgValue", "urn:sobject.tooling.soap.sforce.com","OrgValue","http://www.w3.org/2001/XMLSchema","string",0,1,true), OrgValue, OrgValue__is_set);
    }

    /**
     * element : OrgValueRaw of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean OrgValueRaw__is_set = false;

    private java.lang.String OrgValueRaw;

    public java.lang.String getOrgValueRaw() {
      return OrgValueRaw;
    }

    public void setOrgValueRaw(java.lang.String OrgValueRaw) {
      this.OrgValueRaw = OrgValueRaw;
      OrgValueRaw__is_set = true;
    }

    protected void setOrgValueRaw(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("OrgValueRaw", "urn:sobject.tooling.soap.sforce.com","OrgValueRaw","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setOrgValueRaw(__typeMapper.readString(__in, _lookupTypeInfo("OrgValueRaw", "urn:sobject.tooling.soap.sforce.com","OrgValueRaw","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldOrgValueRaw(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("OrgValueRaw", "urn:sobject.tooling.soap.sforce.com","OrgValueRaw","http://www.w3.org/2001/XMLSchema","string",0,1,true), OrgValueRaw, OrgValueRaw__is_set);
    }

    /**
     * element : RiskType of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean RiskType__is_set = false;

    private java.lang.String RiskType;

    public java.lang.String getRiskType() {
      return RiskType;
    }

    public void setRiskType(java.lang.String RiskType) {
      this.RiskType = RiskType;
      RiskType__is_set = true;
    }

    protected void setRiskType(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("RiskType", "urn:sobject.tooling.soap.sforce.com","RiskType","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setRiskType(__typeMapper.readString(__in, _lookupTypeInfo("RiskType", "urn:sobject.tooling.soap.sforce.com","RiskType","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldRiskType(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("RiskType", "urn:sobject.tooling.soap.sforce.com","RiskType","http://www.w3.org/2001/XMLSchema","string",0,1,true), RiskType, RiskType__is_set);
    }

    /**
     * element : SecurityHealthCheck of type {urn:sobject.tooling.soap.sforce.com}SecurityHealthCheck
     * java type: com.sforce.soap.tooling.sobject.SecurityHealthCheck
     */
    private boolean SecurityHealthCheck__is_set = false;

    private com.sforce.soap.tooling.sobject.SecurityHealthCheck SecurityHealthCheck;

    public com.sforce.soap.tooling.sobject.SecurityHealthCheck getSecurityHealthCheck() {
      return SecurityHealthCheck;
    }

    public void setSecurityHealthCheck(com.sforce.soap.tooling.sobject.SecurityHealthCheck SecurityHealthCheck) {
      this.SecurityHealthCheck = SecurityHealthCheck;
      SecurityHealthCheck__is_set = true;
    }

    protected void setSecurityHealthCheck(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("SecurityHealthCheck", "urn:sobject.tooling.soap.sforce.com","SecurityHealthCheck","urn:sobject.tooling.soap.sforce.com","SecurityHealthCheck",0,1,true))) {
        setSecurityHealthCheck((com.sforce.soap.tooling.sobject.SecurityHealthCheck)__typeMapper.readObject(__in, _lookupTypeInfo("SecurityHealthCheck", "urn:sobject.tooling.soap.sforce.com","SecurityHealthCheck","urn:sobject.tooling.soap.sforce.com","SecurityHealthCheck",0,1,true), com.sforce.soap.tooling.sobject.SecurityHealthCheck.class));
      }
    }

    private void writeFieldSecurityHealthCheck(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("SecurityHealthCheck", "urn:sobject.tooling.soap.sforce.com","SecurityHealthCheck","urn:sobject.tooling.soap.sforce.com","SecurityHealthCheck",0,1,true), SecurityHealthCheck, SecurityHealthCheck__is_set);
    }

    /**
     * element : SecurityHealthCheckId of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean SecurityHealthCheckId__is_set = false;

    private java.lang.String SecurityHealthCheckId;

    public java.lang.String getSecurityHealthCheckId() {
      return SecurityHealthCheckId;
    }

    public void setSecurityHealthCheckId(java.lang.String SecurityHealthCheckId) {
      this.SecurityHealthCheckId = SecurityHealthCheckId;
      SecurityHealthCheckId__is_set = true;
    }

    protected void setSecurityHealthCheckId(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("SecurityHealthCheckId", "urn:sobject.tooling.soap.sforce.com","SecurityHealthCheckId","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setSecurityHealthCheckId(__typeMapper.readString(__in, _lookupTypeInfo("SecurityHealthCheckId", "urn:sobject.tooling.soap.sforce.com","SecurityHealthCheckId","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldSecurityHealthCheckId(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("SecurityHealthCheckId", "urn:sobject.tooling.soap.sforce.com","SecurityHealthCheckId","http://www.w3.org/2001/XMLSchema","string",0,1,true), SecurityHealthCheckId, SecurityHealthCheckId__is_set);
    }

    /**
     * element : Setting of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean Setting__is_set = false;

    private java.lang.String Setting;

    public java.lang.String getSetting() {
      return Setting;
    }

    public void setSetting(java.lang.String Setting) {
      this.Setting = Setting;
      Setting__is_set = true;
    }

    protected void setSetting(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("Setting", "urn:sobject.tooling.soap.sforce.com","Setting","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setSetting(__typeMapper.readString(__in, _lookupTypeInfo("Setting", "urn:sobject.tooling.soap.sforce.com","Setting","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldSetting(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("Setting", "urn:sobject.tooling.soap.sforce.com","Setting","http://www.w3.org/2001/XMLSchema","string",0,1,true), Setting, Setting__is_set);
    }

    /**
     * element : SettingGroup of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean SettingGroup__is_set = false;

    private java.lang.String SettingGroup;

    public java.lang.String getSettingGroup() {
      return SettingGroup;
    }

    public void setSettingGroup(java.lang.String SettingGroup) {
      this.SettingGroup = SettingGroup;
      SettingGroup__is_set = true;
    }

    protected void setSettingGroup(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("SettingGroup", "urn:sobject.tooling.soap.sforce.com","SettingGroup","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setSettingGroup(__typeMapper.readString(__in, _lookupTypeInfo("SettingGroup", "urn:sobject.tooling.soap.sforce.com","SettingGroup","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldSettingGroup(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("SettingGroup", "urn:sobject.tooling.soap.sforce.com","SettingGroup","http://www.w3.org/2001/XMLSchema","string",0,1,true), SettingGroup, SettingGroup__is_set);
    }

    /**
     * element : SettingRiskCategory of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean SettingRiskCategory__is_set = false;

    private java.lang.String SettingRiskCategory;

    public java.lang.String getSettingRiskCategory() {
      return SettingRiskCategory;
    }

    public void setSettingRiskCategory(java.lang.String SettingRiskCategory) {
      this.SettingRiskCategory = SettingRiskCategory;
      SettingRiskCategory__is_set = true;
    }

    protected void setSettingRiskCategory(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("SettingRiskCategory", "urn:sobject.tooling.soap.sforce.com","SettingRiskCategory","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setSettingRiskCategory(__typeMapper.readString(__in, _lookupTypeInfo("SettingRiskCategory", "urn:sobject.tooling.soap.sforce.com","SettingRiskCategory","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldSettingRiskCategory(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("SettingRiskCategory", "urn:sobject.tooling.soap.sforce.com","SettingRiskCategory","http://www.w3.org/2001/XMLSchema","string",0,1,true), SettingRiskCategory, SettingRiskCategory__is_set);
    }

    /**
     * element : StandardValue of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean StandardValue__is_set = false;

    private java.lang.String StandardValue;

    public java.lang.String getStandardValue() {
      return StandardValue;
    }

    public void setStandardValue(java.lang.String StandardValue) {
      this.StandardValue = StandardValue;
      StandardValue__is_set = true;
    }

    protected void setStandardValue(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("StandardValue", "urn:sobject.tooling.soap.sforce.com","StandardValue","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setStandardValue(__typeMapper.readString(__in, _lookupTypeInfo("StandardValue", "urn:sobject.tooling.soap.sforce.com","StandardValue","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldStandardValue(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("StandardValue", "urn:sobject.tooling.soap.sforce.com","StandardValue","http://www.w3.org/2001/XMLSchema","string",0,1,true), StandardValue, StandardValue__is_set);
    }

    /**
     * element : StandardValueRaw of type {http://www.w3.org/2001/XMLSchema}string
     * java type: java.lang.String
     */
    private boolean StandardValueRaw__is_set = false;

    private java.lang.String StandardValueRaw;

    public java.lang.String getStandardValueRaw() {
      return StandardValueRaw;
    }

    public void setStandardValueRaw(java.lang.String StandardValueRaw) {
      this.StandardValueRaw = StandardValueRaw;
      StandardValueRaw__is_set = true;
    }

    protected void setStandardValueRaw(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __in.peekTag();
      if (__typeMapper.isElement(__in, _lookupTypeInfo("StandardValueRaw", "urn:sobject.tooling.soap.sforce.com","StandardValueRaw","http://www.w3.org/2001/XMLSchema","string",0,1,true))) {
        setStandardValueRaw(__typeMapper.readString(__in, _lookupTypeInfo("StandardValueRaw", "urn:sobject.tooling.soap.sforce.com","StandardValueRaw","http://www.w3.org/2001/XMLSchema","string",0,1,true), java.lang.String.class));
      }
    }

    private void writeFieldStandardValueRaw(com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      __typeMapper.writeObject(__out, _lookupTypeInfo("StandardValueRaw", "urn:sobject.tooling.soap.sforce.com","StandardValueRaw","http://www.w3.org/2001/XMLSchema","string",0,1,true), StandardValueRaw, StandardValueRaw__is_set);
    }

    /**
     */
    @Override
    public void write(javax.xml.namespace.QName __element,
        com.sforce.ws.parser.XmlOutputStream __out, com.sforce.ws.bind.TypeMapper __typeMapper)
        throws java.io.IOException {
      __out.writeStartTag(__element.getNamespaceURI(), __element.getLocalPart());
      __typeMapper.writeXsiType(__out, "urn:sobject.tooling.soap.sforce.com", "SecurityHealthCheckRisks");
      writeFields(__out, __typeMapper);
      __out.writeEndTag(__element.getNamespaceURI(), __element.getLocalPart());
    }

    protected void writeFields(com.sforce.ws.parser.XmlOutputStream __out,
         com.sforce.ws.bind.TypeMapper __typeMapper)
         throws java.io.IOException {
       super.writeFields(__out, __typeMapper);
       writeFields1(__out, __typeMapper);
    }

    @Override
    public void load(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      __typeMapper.consumeStartTag(__in);
      loadFields(__in, __typeMapper);
      __typeMapper.consumeEndTag(__in);
    }

    protected void loadFields(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
        super.loadFields(__in, __typeMapper);
        loadFields1(__in, __typeMapper);
    }

    @Override
    public String toString() {
      java.lang.StringBuilder sb = new java.lang.StringBuilder();
      sb.append("[SecurityHealthCheckRisks ");
      sb.append(super.toString());
      toString1(sb);

      sb.append("]\n");
      return sb.toString();
    }

    private void toStringHelper(StringBuilder sb, String name, Object value) {
      sb.append(' ').append(name).append("='").append(com.sforce.ws.util.Verbose.toString(value)).append("'\n");
    }

    private void writeFields1(com.sforce.ws.parser.XmlOutputStream __out,
         com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException {
      writeFieldDurableId(__out, __typeMapper);
      writeFieldOrgValue(__out, __typeMapper);
      writeFieldOrgValueRaw(__out, __typeMapper);
      writeFieldRiskType(__out, __typeMapper);
      writeFieldSecurityHealthCheck(__out, __typeMapper);
      writeFieldSecurityHealthCheckId(__out, __typeMapper);
      writeFieldSetting(__out, __typeMapper);
      writeFieldSettingGroup(__out, __typeMapper);
      writeFieldSettingRiskCategory(__out, __typeMapper);
      writeFieldStandardValue(__out, __typeMapper);
      writeFieldStandardValueRaw(__out, __typeMapper);
    }

    private void loadFields1(com.sforce.ws.parser.XmlInputStream __in,
        com.sforce.ws.bind.TypeMapper __typeMapper) throws java.io.IOException, com.sforce.ws.ConnectionException {
      setDurableId(__in, __typeMapper);
      setOrgValue(__in, __typeMapper);
      setOrgValueRaw(__in, __typeMapper);
      setRiskType(__in, __typeMapper);
      setSecurityHealthCheck(__in, __typeMapper);
      setSecurityHealthCheckId(__in, __typeMapper);
      setSetting(__in, __typeMapper);
      setSettingGroup(__in, __typeMapper);
      setSettingRiskCategory(__in, __typeMapper);
      setStandardValue(__in, __typeMapper);
      setStandardValueRaw(__in, __typeMapper);
    }

    private void toString1(StringBuilder sb) {
      toStringHelper(sb, "DurableId", DurableId);
      toStringHelper(sb, "OrgValue", OrgValue);
      toStringHelper(sb, "OrgValueRaw", OrgValueRaw);
      toStringHelper(sb, "RiskType", RiskType);
      toStringHelper(sb, "SecurityHealthCheck", SecurityHealthCheck);
      toStringHelper(sb, "SecurityHealthCheckId", SecurityHealthCheckId);
      toStringHelper(sb, "Setting", Setting);
      toStringHelper(sb, "SettingGroup", SettingGroup);
      toStringHelper(sb, "SettingRiskCategory", SettingRiskCategory);
      toStringHelper(sb, "StandardValue", StandardValue);
      toStringHelper(sb, "StandardValueRaw", StandardValueRaw);
    }


}