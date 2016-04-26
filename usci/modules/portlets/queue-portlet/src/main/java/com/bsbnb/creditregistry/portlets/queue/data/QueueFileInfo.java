package com.bsbnb.creditregistry.portlets.queue.data;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.CheckBox;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class QueueFileInfo implements Property.ValueChangeListener {

    private String filepath;
    private long length;
    private int userId;
    private int inputInfoId;
    private String statusCode;
    private String status;
    private int protocolCount;
    private int creditorId;
    private String creditorName;
    private int rownum;
    private Date receiverDate;
    private String filename;
    private String shortFilename;

    private final ObjectProperty<Boolean> selectedProperty;
    private CheckBox selectionCheckBox;
    private final List<SelectionStateChangedListener> listeners = new ArrayList<SelectionStateChangedListener>();

    public QueueFileInfo() {
        selectedProperty = new ObjectProperty<Boolean>(false);
        selectedProperty.addListener(this);
    }

    /**
     * @return the filepath
     */
    public String getFilePath() {
        return filepath;
    }

    /**
     * @param filepath the filepath to set
     */
    public void setFilePath(String filepath) {
        this.filepath = filepath;
        length = 0;
        if (filepath != null) {
            File file = new File(filepath);
            if (file.exists()) {
                length = file.length();
            }
        }
    }

    /**
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * @return the inputInfoId
     */
    public int getInputInfoId() {
        return inputInfoId;
    }

    /**
     * @param inputInfoId the inputInfoId to set
     */
    public void setInputInfoId(int inputInfoId) {
        this.inputInfoId = inputInfoId;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the protocolCount
     */
    public int getProtocolCount() {
        return protocolCount;
    }

    /**
     * @param protocolCount the protocolCount to set
     */
    public void setProtocolCount(int protocolCount) {
        this.protocolCount = protocolCount;
    }

    /**
     * @return the organizationName
     */
    public String getCreditorName() {
        return creditorName;
    }

    /**
     * @param organizationName the organizationName to set
     */
    public void setCreditorName(String organizationName) {
        this.creditorName = organizationName;
    }

    /**
     * @return the rownum
     */
    public int getRownum() {
        return rownum;
    }

    /**
     * @param rownum the rownum to set
     */
    public void setRownum(int rownum) {
        this.rownum = rownum;
    }

    /**
     * @return the receiverDate
     */
    public Date getReceiverDate() {
        return receiverDate;
    }

    /**
     * @param receiverDate the receiverDate to set
     */
    public void setReceiverDate(Date receiverDate) {
        this.receiverDate = receiverDate;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
    
    public String getShortFilename() {
        return shortFilename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
        if(filename!=null&&filename.length()>30) {
            this.shortFilename = filename.substring(0, 9)+"..."+filename.substring(filename.length()-8);
        } else {
            this.shortFilename = filename;
        }
    }

    /**
     * @return the statusCode
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return the creditorId
     */
    public int getCreditorId() {
        return creditorId;
    }

    /**
     * @param creditorId the creditorId to set
     */
    public void setCreditorId(int creditorId) {
        this.creditorId = creditorId;
    }

    public boolean hasFile() {
        return filepath != null;
    }

    /**
     * @return the userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the selected
     */
    public boolean isSelected() {
        return selectedProperty.getValue();
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        selectedProperty.setValue(selected);
    }

    /**
     * @return the selectionCheckBox
     */
    public CheckBox getSelectionCheckBox() {
        if (selectionCheckBox == null) {
            selectionCheckBox = new CheckBox(null, selectedProperty);
            selectionCheckBox.setImmediate(true);
        }
        return selectionCheckBox;
    }

    public void addSelectionStateChangedListener(SelectionStateChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        for (SelectionStateChangedListener listener : listeners) {
            listener.selectionStateChanged(this);
        }
    }

    public interface SelectionStateChangedListener {

        public void selectionStateChanged(QueueFileInfo sender);
    }
}
