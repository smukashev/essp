package kz.bsbnb.usci.portlet.report.test;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.*;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.expando.model.ExpandoBridge;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.portlet.report.data.*;
import com.mockrunner.mock.jdbc.MockResultSet;

import java.io.Serializable;
import java.util.List;
import  java.util.*;
import java.sql.ResultSet;
import java.util.List;
/**
 * Created by Bauyrzhan.Ibraimov on 15.06.2015.
 */
public class Test {
    Date repdate;
    Long creditorId;
    public  Test(Long creditorId, Date repdate)
    {
        this.repdate=repdate;
        this.creditorId=creditorId;
    }
    public ResultSet getData()  {
        List<String> headers = new ArrayList<String>();
        headers.add("File_name");
        headers.add("receiver_date");
        headers.add("protocol_type_description");
        headers.add("primary_contract_date");
        headers.add("protocol_type");
        headers.add("message_type");
        headers.add("message");
        headers.add("note");
        List<List<Object>> data = new ArrayList<List<Object>>();
        ResultSet rs=null;
        DataProvider provider = new BeanDataProvider();
        HashMap<Long, Creditor> inputCreditors = new HashMap<Long, Creditor>();
        User user = new User() {
            @Override
            public List<Address> getAddresses() throws SystemException {
                return null;
            }

            @Override
            public Date getBirthday() throws PortalException, SystemException {
                return null;
            }

            @Override
            public String getCompanyMx() throws PortalException, SystemException {
                return null;
            }

            @Override
            public Contact getContact() throws PortalException, SystemException {
                return null;
            }

            @Override
            public String getDigest() {
                return null;
            }

            @Override
            public String getDigest(String s) {
                return null;
            }

            @Override
            public String getDisplayEmailAddress() {
                return null;
            }

            @Override
            public String getDisplayURL(String s, String s1) throws PortalException, SystemException {
                return null;
            }

            @Override
            public String getDisplayURL(String s, String s1, boolean b) throws PortalException, SystemException {
                return null;
            }

            @Override
            public String getDisplayURL(ThemeDisplay themeDisplay) throws PortalException, SystemException {
                return null;
            }

            @Override
            public String getDisplayURL(ThemeDisplay themeDisplay, boolean b) throws PortalException, SystemException {
                return null;
            }

            @Override
            public List<EmailAddress> getEmailAddresses() throws SystemException {
                return null;
            }

            @Override
            public boolean getFemale() throws PortalException, SystemException {
                return false;
            }

            @Override
            public String getFullName() {
                return null;
            }

            @Override
            public Group getGroup() throws PortalException, SystemException {
                return null;
            }

            @Override
            public long getGroupId() throws PortalException, SystemException {
                return 0;
            }

            @Override
            public long[] getGroupIds() throws PortalException, SystemException {
                return new long[0];
            }

            @Override
            public List<Group> getGroups() throws PortalException, SystemException {
                return null;
            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String getLogin() throws PortalException, SystemException {
                return null;
            }

            @Override
            public boolean getMale() throws PortalException, SystemException {
                return false;
            }

            @Override
            public List<Group> getMySites() throws PortalException, SystemException {
                return null;
            }

            @Override
            public List<Group> getMySites(boolean b, int i) throws PortalException, SystemException {
                return null;
            }

            @Override
            public List<Group> getMySites(int i) throws PortalException, SystemException {
                return null;
            }

            @Override
            public List<Group> getMySites(String[] strings, boolean b, int i) throws PortalException, SystemException {
                return null;
            }

            @Override
            public List<Group> getMySites(String[] strings, int i) throws PortalException, SystemException {
                return null;
            }

            @Override
            public long[] getOrganizationIds() throws PortalException, SystemException {
                return new long[0];
            }

            @Override
            public long[] getOrganizationIds(boolean b) throws PortalException, SystemException {
                return new long[0];
            }

            @Override
            public List<Organization> getOrganizations() throws PortalException, SystemException {
                return null;
            }

            @Override
            public List<Organization> getOrganizations(boolean b) throws PortalException, SystemException {
                return null;
            }

            @Override
            public boolean getPasswordModified() {
                return false;
            }

            @Override
            public PasswordPolicy getPasswordPolicy() throws PortalException, SystemException {
                return null;
            }

            @Override
            public String getPasswordUnencrypted() {
                return null;
            }

            @Override
            public List<Phone> getPhones() throws SystemException {
                return null;
            }

            @Override
            public String getPortraitURL(ThemeDisplay themeDisplay) throws PortalException, SystemException {
                return null;
            }

            @Override
            public int getPrivateLayoutsPageCount() throws PortalException, SystemException {
                return 0;
            }

            @Override
            public int getPublicLayoutsPageCount() throws PortalException, SystemException {
                return 0;
            }

            @Override
            public Set<String> getReminderQueryQuestions() throws PortalException, SystemException {
                return null;
            }

            @Override
            public long[] getRoleIds() throws SystemException {
                return new long[0];
            }

            @Override
            public List<Role> getRoles() throws SystemException {
                return null;
            }

            @Override
            public long[] getTeamIds() throws SystemException {
                return new long[0];
            }

            @Override
            public List<Team> getTeams() throws SystemException {
                return null;
            }

            @Override
            public TimeZone getTimeZone() {
                return null;
            }

            @Override
            public long[] getUserGroupIds() throws SystemException {
                return new long[0];
            }

            @Override
            public List<UserGroup> getUserGroups() throws SystemException {
                return null;
            }

            @Override
            public List<Website> getWebsites() throws SystemException {
                return null;
            }

            @Override
            public boolean hasCompanyMx() throws PortalException, SystemException {
                return false;
            }

            @Override
            public boolean hasCompanyMx(String s) throws PortalException, SystemException {
                return false;
            }

            @Override
            public boolean hasMySites() throws PortalException, SystemException {
                return false;
            }

            @Override
            public boolean hasOrganization() throws PortalException, SystemException {
                return false;
            }

            @Override
            public boolean hasPrivateLayouts() throws PortalException, SystemException {
                return false;
            }

            @Override
            public boolean hasPublicLayouts() throws PortalException, SystemException {
                return false;
            }

            @Override
            public boolean hasReminderQuery() {
                return false;
            }

            @Override
            public boolean isActive() {
                return false;
            }

            @Override
            public boolean isFemale() throws PortalException, SystemException {
                return false;
            }

            @Override
            public boolean isMale() throws PortalException, SystemException {
                return false;
            }

            @Override
            public boolean isPasswordModified() {
                return false;
            }

            @Override
            public void setLanguageId(String s) {

            }

            @Override
            public void setPasswordModified(boolean b) {

            }

            @Override
            public void setPasswordUnencrypted(String s) {

            }

            @Override
            public void setTimeZoneId(String s) {

            }

            @Override
            public void persist() throws SystemException {

            }

            @Override
            public long getPrimaryKey() {
                return 0;
            }

            @Override
            public void setPrimaryKey(long l) {

            }

            @Override
            public String getUuid() {
                return null;
            }

            @Override
            public void setUuid(String s) {

            }

            @Override
            public long getUserId() {
                return 0;
            }

            @Override
            public void setUserId(long l) {

            }

            @Override
            public String getUserUuid() throws SystemException {
                return null;
            }

            @Override
            public void setUserUuid(String s) {

            }

            @Override
            public long getCompanyId() {
                return 0;
            }

            @Override
            public void setCompanyId(long l) {

            }

            @Override
            public Date getCreateDate() {
                return null;
            }

            @Override
            public void setCreateDate(Date date) {

            }

            @Override
            public Date getModifiedDate() {
                return null;
            }

            @Override
            public void setModifiedDate(Date date) {

            }

            @Override
            public boolean getDefaultUser() {
                return false;
            }

            @Override
            public boolean isDefaultUser() {
                return false;
            }

            @Override
            public void setDefaultUser(boolean b) {

            }

            @Override
            public long getContactId() {
                return 0;
            }

            @Override
            public void setContactId(long l) {

            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public void setPassword(String s) {

            }

            @Override
            public boolean getPasswordEncrypted() {
                return false;
            }

            @Override
            public boolean isPasswordEncrypted() {
                return false;
            }

            @Override
            public void setPasswordEncrypted(boolean b) {

            }

            @Override
            public boolean getPasswordReset() {
                return false;
            }

            @Override
            public boolean isPasswordReset() {
                return false;
            }

            @Override
            public void setPasswordReset(boolean b) {

            }

            @Override
            public Date getPasswordModifiedDate() {
                return null;
            }

            @Override
            public void setPasswordModifiedDate(Date date) {

            }

            @Override
            public void setDigest(String s) {

            }

            @Override
            public String getReminderQueryQuestion() {
                return null;
            }

            @Override
            public void setReminderQueryQuestion(String s) {

            }

            @Override
            public String getReminderQueryAnswer() {
                return null;
            }

            @Override
            public void setReminderQueryAnswer(String s) {

            }

            @Override
            public int getGraceLoginCount() {
                return 0;
            }

            @Override
            public void setGraceLoginCount(int i) {

            }

            @Override
            public String getScreenName() {
                return null;
            }

            @Override
            public void setScreenName(String s) {

            }

            @Override
            public String getEmailAddress() {
                return null;
            }

            @Override
            public void setEmailAddress(String s) {

            }

            @Override
            public long getFacebookId() {
                return 0;
            }

            @Override
            public void setFacebookId(long l) {

            }

            @Override
            public String getOpenId() {
                return null;
            }

            @Override
            public void setOpenId(String s) {

            }

            @Override
            public long getPortraitId() {
                return 0;
            }

            @Override
            public void setPortraitId(long l) {

            }

            @Override
            public String getLanguageId() {
                return null;
            }

            @Override
            public String getTimeZoneId() {
                return null;
            }

            @Override
            public String getGreeting() {
                return null;
            }

            @Override
            public void setGreeting(String s) {

            }

            @Override
            public String getComments() {
                return null;
            }

            @Override
            public void setComments(String s) {

            }

            @Override
            public String getFirstName() {
                return null;
            }

            @Override
            public void setFirstName(String s) {

            }

            @Override
            public String getMiddleName() {
                return null;
            }

            @Override
            public void setMiddleName(String s) {

            }

            @Override
            public String getLastName() {
                return null;
            }

            @Override
            public void setLastName(String s) {

            }

            @Override
            public String getJobTitle() {
                return null;
            }

            @Override
            public void setJobTitle(String s) {

            }

            @Override
            public Date getLoginDate() {
                return null;
            }

            @Override
            public void setLoginDate(Date date) {

            }

            @Override
            public String getLoginIP() {
                return null;
            }

            @Override
            public void setLoginIP(String s) {

            }

            @Override
            public Date getLastLoginDate() {
                return null;
            }

            @Override
            public void setLastLoginDate(Date date) {

            }

            @Override
            public String getLastLoginIP() {
                return null;
            }

            @Override
            public void setLastLoginIP(String s) {

            }

            @Override
            public Date getLastFailedLoginDate() {
                return null;
            }

            @Override
            public void setLastFailedLoginDate(Date date) {

            }

            @Override
            public int getFailedLoginAttempts() {
                return 0;
            }

            @Override
            public void setFailedLoginAttempts(int i) {

            }

            @Override
            public boolean getLockout() {
                return false;
            }

            @Override
            public boolean isLockout() {
                return false;
            }

            @Override
            public void setLockout(boolean b) {

            }

            @Override
            public Date getLockoutDate() {
                return null;
            }

            @Override
            public void setLockoutDate(Date date) {

            }

            @Override
            public boolean getAgreedToTermsOfUse() {
                return false;
            }

            @Override
            public boolean isAgreedToTermsOfUse() {
                return false;
            }

            @Override
            public void setAgreedToTermsOfUse(boolean b) {

            }

            @Override
            public boolean getEmailAddressVerified() {
                return false;
            }

            @Override
            public boolean isEmailAddressVerified() {
                return false;
            }

            @Override
            public void setEmailAddressVerified(boolean b) {

            }

            @Override
            public int getStatus() {
                return 0;
            }

            @Override
            public void setStatus(int i) {

            }

            @Override
            public boolean isNew() {
                return false;
            }

            @Override
            public void setNew(boolean b) {

            }

            @Override
            public boolean isCachedModel() {
                return false;
            }

            @Override
            public void setCachedModel(boolean b) {

            }

            @Override
            public boolean isEscapedModel() {
                return false;
            }

            @Override
            public Serializable getPrimaryKeyObj() {
                return null;
            }

            @Override
            public void setPrimaryKeyObj(Serializable serializable) {

            }

            @Override
            public ExpandoBridge getExpandoBridge() {
                return null;
            }

            @Override
            public void setExpandoBridgeAttributes(ServiceContext serviceContext) {

            }

            @Override
            public int compareTo(User user) {
                return 0;
            }

            @Override
            public CacheModel<User> toCacheModel() {
                return null;
            }

            @Override
            public User toEscapedModel() {
                return null;
            }

            @Override
            public User toUnescapedModel() {
                return null;
            }

            @Override
            public String toXmlString() {
                return null;
            }

            @Override
            public Map<String, Object> getModelAttributes() {
                return null;
            }

            @Override
            public void resetOriginalValues() {

            }

            @Override
            public void setModelAttributes(Map<String, Object> map) {

            }

            @Override
            public Class<?> getModelClass() {
                return null;
            }

            @Override
            public String getModelClassName() {
                return null;
            }

            @Override
            public Object clone() {
                return null;
            }
        };
        List<Creditor> creditors = provider.getCreditorsList(user);
        for(Creditor cred : creditors) {
            inputCreditors.put(cred.getId(), cred);
        }
        Creditor currentCreditor = inputCreditors.get(creditorId);
        creditors.clear();
        creditors.add(currentCreditor);

        List<InputInfoDisplayBean> list  =provider.getInputInfosByCreditors(creditors, repdate);

        List<ProtocolDisplayBean> protlist;
        for(InputInfoDisplayBean info : list)
        {
            protlist = provider.getProtocolsByInputInfo(info);

            for(ProtocolDisplayBean protbean: protlist)
            {
                Protocol protocol = protbean.getProtocol();
                List<Object> Fields = new ArrayList<Object>();
                Fields.add(protocol.getInputInfo().getFileName());
                Fields.add(protocol.getInputInfo().getReceiverDate());
                Fields.add(protocol.getTypeDescription());
                Fields.add(protocol.getPrimaryContractDate());
                Fields.add(protocol.getProtocolType().getType());
                Fields.add(protocol.getMessageType().getNameRu());
                Fields.add(protocol.getMessage().getNameKz());
                Fields.add(protocol.getNote());
                data.add(Fields);
            }

        }
       try {
            rs = getResultSet(headers, data);
       }
       catch (Exception ex)
       {}
        return  rs;
    }

    public ResultSet getResultSet(List<String> headers, List<List<Object>> data) throws Exception {

        // validation
        if (headers == null || data == null) {
            throw new Exception(Errors.compose(Errors.E255));
        }

      //  if (headers.size() != data.size()) {
       //     throw new Exception("parameters size are not equals");
       // }

        // create a mock result set
        MockResultSet mockResultSet = new MockResultSet("myResultSet");

        // add header
        for (String string : headers) {
            mockResultSet.addColumn(string);
        }

        // add data
        for (List<Object> list : data) {
            mockResultSet.addRow(list);
        }

        return mockResultSet;
    }
}

