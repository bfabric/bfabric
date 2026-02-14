/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.xml.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Container;
import org.bfabric.entity.Offer;
import org.bfabric.entity.Order;
import org.bfabric.entity.Project;

@XmlRootElement(name = "order")
public class XMLOrder extends XMLContainer {

    @XmlElement
    private Boolean biosafetylevel2precautionsrequired;

    @XmlElement
    private String consumable;

    @XmlElement
    private Integer countorderitems;

    @XmlElement
    private Boolean customOption;

    @XmlElement
    private Boolean customPrimer;

    @XmlElement
    private Boolean darkCycle;

    @XmlElement
    private Boolean dataproduced;

    @XmlElement
    private String demultiplexing;

    @XmlElement
    private Boolean express;

    @XmlElement
    private String fastasequence;

    @XmlElement
    private BigDecimal hoursrequested;

    @XmlElement
    private String index1;

    @XmlElement
    private String index2;

    @XmlElement
    private Boolean initialcustomstatus;

    @XmlElement
    private Integer insertsize;

    @XmlElement
    private String instrument;

    @XmlElement
    private String instrumentdatadelivery;

    @XmlElement
    private String instrumentdatapackage;

    @XmlElement
    private String instrumentreadconfiguration;

    @XmlElement
    private String kitsused;

    @XmlElement
    private String libraryprotocol;

    @XmlElement
    private List<String> libraryprotocoloption = new ArrayList<>();

    @XmlElement
    private String mailtrackingnumber;

    @XmlElement
    private Boolean nuclei;

    @XmlElement
    private Integer numberOfCyclesRead1;

    @XmlElement
    private Integer numberOfCyclesRead2;

    @XmlElement
    private BigDecimal numberofcellsnuclei;

    @XmlElement
    private Integer numberofchips;

    @XmlElement
    private Integer numberofplates;

    @XmlElement
    private String numberofreplicates;

    @XmlElement
    private Integer numberofrunssequencing;

    @XmlElement
    private Integer numberofrunstapestation;

    @XmlElement
    private Integer numberofsamples;

    @XmlElement
    private Long offer;

    @XmlElement
    private List<String> offerid = new ArrayList<>();

    @XmlElement
    private Long oldprojectorderid;

    @XmlElement
    private Long oldserviceorderid;

    @XmlElement
    private BigDecimal phiX;

    @XmlElement
    private Integer platesubmissionproposallimit;

    @XmlElement
    private Boolean processesplates;

    @XmlElement
    private Boolean processessamples;

    @XmlAttribute
    private String projectid;

    @XmlElement
    private String read1;

    @XmlElement
    private String read2;

    @XmlElement
    private String remarks;

    @XmlElement
    private Boolean requestBclFile;

    @XmlElement
    private Boolean requiresproject;

    @XmlElement
    private String sampleretention;

    @XmlElement
    private Boolean samplescontaintransgenes;

    @XmlElement
    private String sampletype;

    @XmlElement
    private String sequencingapplication;

    @XmlElement
    private String sequencingapplicationindexlength;

    @XmlElement
    private Boolean servicecolumnenabled;

    @XmlElement
    private XMLServiceType servicetype;

    @XmlElement
    private String storagemodel;

    @XmlElement
    private String summary;

    @XmlElement
    private BigDecimal totalnumberofinstrumentdatapackages;

    @XmlElement
    private BigDecimal userbenchusage;

    public XMLOrder() {
    }

    public XMLOrder(Container entity, boolean reference) {
        super(entity, reference);
        if (entity != null && reference) {
            Project parent = entity.getProject();
            if (parent != null) {
                setProjectid(parent.getIdString());
            }
        }
    }

    public XMLOrder(Order entity) {
        super(entity);
        if (entity != null) {
            setOldprojectorderid(entity.getOldProjectOrderId());
            setOldserviceorderid(entity.getOldServiceOrderId());
            if (entity.getOffers() != null && !entity.getOffers().isEmpty()) {
                for (Offer aOffer : entity.getOffers()) {
                    getOfferid().add(aOffer.getIdString());
                }
            }
            if (entity.getServiceType() != null) {
                setServicetype(new XMLServiceType(entity.getServiceType(), true));
            }
            if (entity.getStorageModel() != null) {
                setStoragemodel(entity.getStorageModel().getName());
            }
            if (entity.getSequencingApplication() != null) {
                setSequencingapplication(entity.getSequencingApplication().getName());
            }
            if (entity.getInstrument() != null) {
                setInstrument(entity.getInstrument().getName());
            }
            if (entity.getInstrumentReadConfiguration() != null) {
                setInstrumentreadconfiguration(entity.getInstrumentReadConfiguration().getName());
            }
            if (entity.getInstrumentDataDelivery() != null) {
                setInstrumentdatadelivery(entity.getInstrumentDataDelivery().getName());
            }
            if (entity.getInstrumentDataPackage() != null) {
                setInstrumentdatapackage(entity.getInstrumentDataPackage().getName());
            }
            if (entity.getLibraryProtocol() != null) {
                setLibraryprotocol(entity.getLibraryProtocol().getName());
            }
            if (entity.getLibraryProtocolOptionValues() != null) {
                entity.getLibraryProtocolOptionValues().forEach((option) -> getLibraryprotocoloption().add(option.getName()));
            }
            if (entity.getNumberOfSamples() != null) {
                setNumberofsamples(entity.getNumberOfSamples());
            }
            if (entity.getNumberOfCellsNuclei() != null) {
                setNumberofcellsnuclei(entity.getNumberOfCellsNuclei());
            }
            if (entity.getNumberOfChips() != null) {
                setNumberofchips(entity.getNumberOfChips());
            }
            if (entity.getCustomOption() != null) {
                setCustomOption(entity.getCustomOption());
            }
            if (entity.getCustomPrimer() != null) {
                setCustomPrimer(entity.getCustomPrimer());
            }
            if (entity.getDarkCycle() != null) {
                setDarkCycle(entity.getDarkCycle());
            }
            setExpress(entity.isExpress());
            if (entity.getRequestBclFile() != null) {
                setRequestBclFile(entity.getRequestBclFile());
            }
            if (entity.getPhiX() != null) {
                setPhiX(entity.getPhiX());
            }
            if (entity.getNumberOfCyclesRead1() != null) {
                setNumberOfCyclesRead1(entity.getNumberOfCyclesRead1());
            }
            if (entity.getNumberOfCyclesRead2() != null) {
                setNumberOfCyclesRead2(entity.getNumberOfCyclesRead2());
            }
            if (entity.getIndex1() != null) {
                setIndex1(entity.getIndex1());
            }
            if (entity.getSampleRetention() != null) {
                setSampleretention(entity.getSampleRetention());
            }
            if (entity.getMailTrackingNumber() != null) {
                setMailtrackingnumber(entity.getMailTrackingNumber());
            }
            if (entity.getIndex2() != null) {
                setIndex2(entity.getIndex2());
            }
            if (entity.getRead1() != null) {
                setRead1(entity.getRead1());
            }
            if (entity.getRead2() != null) {
                setRead2(entity.getRead2());
            }
            if (entity.getNumberOfReplicates() != null) {
                setNumberofreplicates(entity.getNumberOfReplicates());
            }
            if (entity.getNumberOfRunsSequencing() != null) {
                setNumberofrunssequencing(entity.getNumberOfRunsSequencing());
            }
            if (entity.getNumberOfRunsTapeStation() != null) {
                setNumberofrunstapestation(entity.getNumberOfSamples());
            }
            if (entity.getSequencingApplicationIndexLength() != null) {
                setSequencingapplicationindexlength(entity.getSequencingApplicationIndexLength().getName());
            }
            if (entity.getTotalNumberOfInstrumentDataPackages() != null) {
                setTotalNumberofInstrumentDataPackages(entity.getTotalNumberOfInstrumentDataPackages());
            }
            if (entity.getRemarks() != null) {
                setRemarks(entity.getRemarks());
            }
            if (entity.getInsertSize() != null) {
                setInsertsize(entity.getInsertSize());
            }
            if (entity.getDemultiplexing() != null) {
                setDemultiplexing(entity.getDemultiplexing().getName());
            }
            if (entity.getConsumable() != null) {
                setConsumable(entity.getConsumable().getName());
            }
            if (entity.getOffer() != null) {
                setOffer(entity.getOffer().getId());
            }
            if (entity.getKitsUsed() != null) {
                setKitsused(entity.getKitsUsed());
            }
            if (entity.getSummary() != null) {
                setSummary(entity.getSummary());
            }
            if (entity.getFastaSequence() != null) {
                setFastasequence(entity.getFastaSequence());
            }
            if (entity.getHoursRequested() != null) {
                setHoursrequested(entity.getHoursRequested());
            }
            if (entity.getUserBenchUsage() != null) {
                setUserbenchusage(entity.getUserBenchUsage());
            }
            if (entity.getOrderItems() != null) {
                setCountorderitems(entity.getOrderItems().size());
            }
            if (entity.getDataProduced() != null) {
                setDataproduced(entity.getDataProduced());
            }
            if (entity.getNuclei() != null) {
                setNuclei(entity.getNuclei());
            }
            setRequiresproject(entity.isRequiresProject());
            setProcessessamples(entity.isProcessesSamples());
            setServicecolumnenabled(entity.isServiceColumnEnabled());
            setProcessesplates(entity.isProcessesPlates());
            setPlatesubmissionproposallimit(entity.getPlateSubmissionProposalLimit());
            setInitialcustomstatus(entity.isInitialCustomStatus());
            setBiosafetylevel2precautionsrequired(entity.getBioSafetyLevel2PrecautionsRequired());
            setSamplescontaintransgenes(entity.getSamplesContainTransgenes());
        }
    }

    public Boolean getBiosafetylevel2precautionsrequired() {
        return biosafetylevel2precautionsrequired;
    }

    public String getConsumable() {
        return consumable;
    }

    public Integer getCountorderitems() {
        return countorderitems;
    }

    public Boolean getCustomOption() {
        return customOption;
    }

    public Boolean getCustomPrimer() {
        return customPrimer;
    }

    public Boolean getDarkCycle() {
        return darkCycle;
    }

    public Boolean getDataproduced() {
        return dataproduced;
    }

    public String getDemultiplexing() {
        return demultiplexing;
    }

    public Boolean getExpress() {
        return express;
    }

    public String getFastasequence() {
        return fastasequence;
    }

    public BigDecimal getHoursrequested() {
        return hoursrequested;
    }

    public String getIndex1() {
        return index1;
    }

    public String getIndex2() {
        return index2;
    }

    public Boolean getInitialcustomstatus() {
        return initialcustomstatus;
    }

    public Integer getInsertsize() {
        return insertsize;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getInstrumentdatadelivery() {
        return instrumentdatadelivery;
    }

    public String getInstrumentdatapackage() {
        return instrumentdatapackage;
    }

    public String getInstrumentreadconfiguration() {
        return instrumentreadconfiguration;
    }

    public String getKitsused() {
        return kitsused;
    }

    public String getLibraryprotocol() {
        return libraryprotocol;
    }

    public List<String> getLibraryprotocoloption() {
        return libraryprotocoloption;
    }

    public String getMailtrackingnumber() {
        return mailtrackingnumber;
    }

    public Boolean getNuclei() {
        return nuclei;
    }

    public Integer getNumberOfCyclesRead1() {
        return numberOfCyclesRead1;
    }

    public Integer getNumberOfCyclesRead2() {
        return numberOfCyclesRead2;
    }

    public BigDecimal getNumberofcellsnuclei() {
        return numberofcellsnuclei;
    }

    public Integer getNumberofchips() {
        return numberofchips;
    }

    public Integer getNumberofplates() {
        return numberofplates;
    }

    public String getNumberofreplicates() {
        return numberofreplicates;
    }

    public Integer getNumberofrunssequencing() {
        return numberofrunssequencing;
    }

    public Integer getNumberofrunstapestation() {
        return numberofrunstapestation;
    }

    public Integer getNumberofsamples() {
        return numberofsamples;
    }

    public Long getOffer() {
        return offer;
    }

    public List<String> getOfferid() {
        return offerid;
    }

    public Long getOldprojectorderid() {
        return oldprojectorderid;
    }

    public Long getOldserviceorderid() {
        return oldserviceorderid;
    }

    public BigDecimal getPhiX() {
        return phiX;
    }

    public Integer getPlatesubmissionproposallimit() {
        return platesubmissionproposallimit;
    }

    public Boolean getProcessesplates() {
        return processesplates;
    }

    public Boolean getProcessessamples() {
        return processessamples;
    }

    public String getProjectid() {
        return projectid;
    }

    public String getRead1() {
        return read1;
    }

    public String getRead2() {
        return read2;
    }

    public String getRemarks() {
        return remarks;
    }

    public Boolean getRequestBclFile() {
        return requestBclFile;
    }

    public Boolean getRequiresproject() {
        return requiresproject;
    }

    public String getSampleretention() {
        return sampleretention;
    }

    public Boolean getSamplescontaintransgenes() {
        return samplescontaintransgenes;
    }

    public String getSampletype() {
        return sampletype;
    }

    public String getSequencingapplication() {
        return sequencingapplication;
    }

    public String getSequencingapplicationindexlength() {
        return sequencingapplicationindexlength;
    }

    public Boolean getServicecolumnenabled() {
        return servicecolumnenabled;
    }

    public XMLServiceType getServicetype() {
        return servicetype;
    }

    public String getStoragemodel() {
        return storagemodel;
    }

    public String getSummary() {
        return summary;
    }

    public BigDecimal getTotalNumberofInstrumentDataPackages() {
        return totalnumberofinstrumentdatapackages;
    }

    public BigDecimal getTotalnumberofinstrumentdatapackages() {
        return totalnumberofinstrumentdatapackages;
    }

    public BigDecimal getUserbenchusage() {
        return userbenchusage;
    }

    public void setBiosafetylevel2precautionsrequired(Boolean biosafetylevel2precautionsrequired) {
        this.biosafetylevel2precautionsrequired = biosafetylevel2precautionsrequired;
    }

    public void setConsumable(String consumable) {
        this.consumable = consumable;
    }

    public void setCountorderitems(Integer countorderitems) {
        this.countorderitems = countorderitems;
    }

    public void setCustomOption(Boolean customOption) {
        this.customOption = customOption;
    }

    public void setCustomPrimer(Boolean customPrimer) {
        this.customPrimer = customPrimer;
    }

    public void setDarkCycle(Boolean darkCycle) {
        this.darkCycle = darkCycle;
    }

    public void setDataproduced(Boolean dataproduced) {
        this.dataproduced = dataproduced;
    }

    public void setDemultiplexing(String demultiplexing) {
        this.demultiplexing = demultiplexing;
    }

    public void setExpress(Boolean express) {
        this.express = express;
    }

    public void setFastasequence(String fastasequence) {
        this.fastasequence = fastasequence;
    }

    public void setHoursrequested(BigDecimal hoursrequested) {
        this.hoursrequested = hoursrequested;
    }

    public void setIndex1(String index1) {
        this.index1 = index1;
    }

    public void setIndex2(String index2) {
        this.index2 = index2;
    }

    public void setInitialcustomstatus(Boolean initialcustomstatus) {
        this.initialcustomstatus = initialcustomstatus;
    }

    public void setInsertsize(Integer insertsize) {
        this.insertsize = insertsize;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public void setInstrumentdatadelivery(String instrumentdatadelivery) {
        this.instrumentdatadelivery = instrumentdatadelivery;
    }

    public void setInstrumentdatapackage(String instrumentdatapackage) {
        this.instrumentdatapackage = instrumentdatapackage;
    }

    public void setInstrumentreadconfiguration(String instrumentreadconfiguration) {
        this.instrumentreadconfiguration = instrumentreadconfiguration;
    }

    public void setKitsused(String kitsused) {
        this.kitsused = kitsused;
    }

    public void setLibraryprotocol(String libraryprotocol) {
        this.libraryprotocol = libraryprotocol;
    }

    public void setLibraryprotocoloption(List<String> libraryprotocoloption) {
        this.libraryprotocoloption = libraryprotocoloption;
    }

    public void setMailtrackingnumber(String mailtrackingnumber) {
        this.mailtrackingnumber = mailtrackingnumber;
    }

    public void setNuclei(Boolean nuclei) {
        this.nuclei = nuclei;
    }

    public void setNumberOfCyclesRead1(Integer numberOfCyclesRead1) {
        this.numberOfCyclesRead1 = numberOfCyclesRead1;
    }

    public void setNumberOfCyclesRead2(Integer numberOfCyclesRead2) {
        this.numberOfCyclesRead2 = numberOfCyclesRead2;
    }

    public void setNumberofcellsnuclei(BigDecimal numberofcellsnuclei) {
        this.numberofcellsnuclei = numberofcellsnuclei;
    }

    public void setNumberofchips(Integer numberofchips) {
        this.numberofchips = numberofchips;
    }

    public void setNumberofplates(Integer numberofplates) {
        this.numberofplates = numberofplates;
    }

    public void setNumberofreplicates(String numberofreplicates) {
        this.numberofreplicates = numberofreplicates;
    }

    public void setNumberofrunssequencing(Integer numberofrunssequencing) {
        this.numberofrunssequencing = numberofrunssequencing;
    }

    public void setNumberofrunstapestation(Integer numberofrunstapestation) {
        this.numberofrunstapestation = numberofrunstapestation;
    }

    public void setNumberofsamples(Integer numberofsamples) {
        this.numberofsamples = numberofsamples;
    }

    public void setOffer(Long offer) {
        this.offer = offer;
    }

    public void setOfferid(List<String> offerid) {
        this.offerid = offerid;
    }

    public void setOldprojectorderid(Long oldprojectorderid) {
        this.oldprojectorderid = oldprojectorderid;
    }

    public void setOldserviceorderid(Long oldserviceorderid) {
        this.oldserviceorderid = oldserviceorderid;
    }

    public void setPhiX(BigDecimal phiX) {
        this.phiX = phiX;
    }

    public void setPlatesubmissionproposallimit(Integer platesubmissionproposallimit) {
        this.platesubmissionproposallimit = platesubmissionproposallimit;
    }

    public void setProcessesplates(Boolean processesplates) {
        this.processesplates = processesplates;
    }

    public void setProcessessamples(Boolean processessamples) {
        this.processessamples = processessamples;
    }

    public void setProjectid(String projectid) {
        this.projectid = projectid;
    }

    public void setRead1(String read1) {
        this.read1 = read1;
    }

    public void setRead2(String read2) {
        this.read2 = read2;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setRequestBclFile(Boolean requestBclFile) {
        this.requestBclFile = requestBclFile;
    }

    public void setRequiresproject(Boolean requiresproject) {
        this.requiresproject = requiresproject;
    }

    public void setSampleretention(String sampleretention) {
        this.sampleretention = sampleretention;
    }

    public void setSamplescontaintransgenes(Boolean samplescontaintransgenes) {
        this.samplescontaintransgenes = samplescontaintransgenes;
    }

    public void setSampletype(String sampletype) {
        this.sampletype = sampletype;
    }

    public void setSequencingapplication(String sequencingapplication) {
        this.sequencingapplication = sequencingapplication;
    }

    public void setSequencingapplicationindexlength(String sequencingapplicationindexlength) {
        this.sequencingapplicationindexlength = sequencingapplicationindexlength;
    }

    public void setServicecolumnenabled(Boolean servicecolumnenabled) {
        this.servicecolumnenabled = servicecolumnenabled;
    }

    public void setServicetype(XMLServiceType servicetype) {
        this.servicetype = servicetype;
    }

    public void setStoragemodel(String storagemodel) {
        this.storagemodel = storagemodel;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setTotalNumberofInstrumentDataPackages(BigDecimal totalnumberofinstrumentdatapackages) {
        this.totalnumberofinstrumentdatapackages = totalnumberofinstrumentdatapackages;
    }

    public void setTotalnumberofinstrumentdatapackages(BigDecimal totalnumberofinstrumentdatapackages) {
        this.totalnumberofinstrumentdatapackages = totalnumberofinstrumentdatapackages;
    }

    public void setUserbenchusage(BigDecimal userbenchusage) {
        this.userbenchusage = userbenchusage;
    }

}
