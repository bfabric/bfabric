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

package org.bfabric.webservice.request.parameter;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveSample extends XMLRequestParameterSaveAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private String age;

    @XmlElement
    private String ageunit;

    @XmlElement
    private String amounteluted;

    @XmlElement
    private String amountinput;

    @XmlElement
    private String amounttotal;

    @XmlElement
    private String ampliconsequence;

    @XmlElement
    private String arraydesignname;

    @XmlElement
    private String asiascale;

    @XmlElement
    private String averagesizeinrange;

    @XmlElement
    private String baitid;

    @XmlElement
    private String beadstype;

    @XmlElement
    private String bias;

    @XmlElement
    private String block;

    @XmlElement
    private String buffer;

    @XmlElement
    private String cellcompartment;

    @XmlElement
    private String cellconcentration;

    @XmlElement
    private String cellline;

    @XmlElement
    private String cellnumbers;

    @XmlElement
    private String cellsize;

    @XmlElement
    private String celltype;

    @XmlElement
    private String cellviability;

    @XmlElement(name = "chemicalmodification")
    private List<String> chemicalmodifications;

    @XmlElement
    private String compoundclass;

    @XmlElement
    private String concentration;

    @XmlElement
    private String concentrationinputqc;

    @XmlElement
    private String concentrationinrange;

    @XmlElement
    private String concentrationloading;

    @XmlElement
    private String concentrationmolar;

    @XmlElement
    private String concentrationmolarinrange;

    @XmlElement
    private String concentrationprotein;

    @XmlElement
    private String condition;

    @XmlElement(required = true)
    private String containerid;

    @XmlElement
    private String correctionrate;

    @XmlElement
    private String coverage;

    @XmlElement
    private String cq;

    @XmlElement
    private String crisprlibrary;

    @XmlElement
    private String developmentstage;

    @XmlElement
    private String digestionprotocol;

    @XmlElement
    private String dilution;

    @XmlElement
    private String diseasestate;

    @XmlElement
    private String dmxflag;

    @XmlElement
    private String dsodn;

    @XmlElement
    private String dv200;

    @XmlElement
    private String effectortype;

    @XmlElement
    private String embeddingmedium;

    @XmlElement(name = "enzyme")
    private List<String> enzymes;

    @XmlElement
    private String expressionsystem;

    @XmlElement
    private String extractionprotocol;

    @XmlElement
    private String extractionprotocolstring;

    @XmlElement
    private String fastqscreen;

    @XmlElement
    private String fixation;

    @XmlElement
    private String fraction;

    @XmlElement
    private String geneticmodification;

    @XmlElement
    private String genomiccoordinates;

    @XmlElement
    private String genotype;

    @XmlElement
    private String groupingvar;

    @XmlElement
    private String growthconditions;

    @XmlElement
    private String guidename;

    @XmlElement
    private String guidesequence;

    @XmlElement
    private String hybridizationprotocol;

    @XmlElement
    private String immunoprecipitationtarget;

    @XmlElement
    private String individualid;

    @XmlElement
    private String initialtimepoint;

    @XmlElement
    private String instrument;

    @XmlElement
    private String instrumentid;

    @XmlElement
    private String instrumentmethod;

    @XmlElement
    private String integritynumber;

    @XmlElement(name = "internalstandard")
    private List<String> internalstandards;

    @XmlElement
    private String irts;

    @XmlElement
    private String label;

    @XmlElement
    private String labelamount;

    @XmlElement
    private String labelingmethod;

    @XmlElement
    private String labelingprotocol;

    @XmlElement
    private String libraryprotocol;

    @XmlElement
    private String libraryselection;

    @XmlElement
    private String librarystatus;

    @XmlElement
    private String librarystrategy;

    @XmlElement
    private String lotinformation;

    @XmlElement
    private String lysisbuffer;

    @XmlElement
    private String matrix;

    @XmlElement
    private String media;

    @XmlElement
    private String molarity;

    @XmlElement
    private String molarityfmol;

    @XmlElement
    private String molaritytarget;

    @XmlElement
    private String molecularweight;

    @XmlElement
    private String multiplexed;

    @XmlElement
    private String multiplexid;

    @XmlElement
    private String multiplexid2;

    @XmlElement
    private String multiplexid2dmx;

    @XmlElement
    private String multiplexiddmx;

    @XmlElement
    private String multiplexkit;

    @XmlElement
    private String multiplexkit2;

    @XmlElement
    private String multiplexkit2id;

    @XmlElement
    private String multiplexkitid;

    @XmlElement
    private String numberofcellsloaded;

    @XmlElement
    private String numberofcycles;

    @XmlElement
    private String onslidemodification;

    @XmlElement
    private String organism;

    @XmlElement
    private String organismpart;

    @XmlElement
    private List<String> parentid;

    @XmlElement
    private String pretreatment;

    @XmlElement
    private String proteinamount;

    @XmlElement
    private String puritya260230;

    @XmlElement
    private String puritya260280;

    @XmlElement
    private String qcpassed;

    @XmlElement
    private String qpcr;

    @XmlElement
    private String qualitycontroltype;

    @XmlElement
    private String qubit;

    @XmlElement
    private String readcount;

    @XmlElement
    private String readcounttotal;

    @XmlElement
    private String remultiplex;

    @XmlElement
    private String remultiplexed;

    @XmlElement
    private String rin;

    @XmlElement
    private String sampleform;

    @XmlElement
    private String samplepreparationprotocol;

    @XmlElement
    private String samplepreparationprotocolid;

    @XmlElement
    private String samplingdate;

    @XmlElement
    private String scanningprotocol;

    @XmlElement(name = "separationtechnique")
    private List<String> separationtechniques;

    @XmlElement
    private String sequencingmethod;

    @XmlElement
    private String sequencingmode;

    @XmlElement
    private String sequencingplatform;

    @XmlElement
    private String sequencingprimer;

    @XmlElement
    private String sex;

    @XmlElement
    private String size;

    @XmlElement
    private String sizeaverage;

    @XmlElement
    private String sizegenomeestimated;

    @XmlElement
    private String sizerange;

    @XmlElement
    private String slidetype;

    @XmlElement
    private String sourcetype;

    @XmlElement
    private String species;

    @XmlElement
    private String status;

    @XmlElement
    private String strain;

    @XmlElement
    private String subjectid;

    @XmlElement
    private String surface;

    @XmlElement
    private String tissue;

    @XmlElement
    private String totalamount;

    @XmlElement
    private String treatment;

    @XmlElement
    private String ts;

    @XmlElement
    private String tubeid;

    @XmlElement(required = true)
    private String type;

    @XmlElement
    private String vector;

    @XmlElement
    private String volume;

    @XmlElement
    private String volumedilutionsample;

    @XmlElement
    private String volumedilutionwater;

    @XmlElement
    private String volumeeluted;

    @XmlElement
    private String volumeinput;

    @XmlElement
    private String volumelysisbuffer;

    @XmlElement
    private String volumemeasured;

    @XmlElement
    private String volumereaction;

    @XmlElement
    private String volumetarget;

    @XmlElement
    private String volumetoaddebt;

    @XmlElement
    private String volumetoaddsample;

    @XmlElement
    private String yield;

    public String getAge() {
        return age;
    }

    public String getAgeunit() {
        return ageunit;
    }

    public String getAmounteluted() {
        return amounteluted;
    }

    public String getAmountinput() {
        return amountinput;
    }

    public String getAmounttotal() {
        return amounttotal;
    }

    public String getAmpliconsequence() {
        return ampliconsequence;
    }

    public String getArraydesignname() {
        return arraydesignname;
    }

    public String getAsiascale() {
        return asiascale;
    }

    public String getAveragesizeinrange() {
        return averagesizeinrange;
    }

    public String getBaitid() {
        return baitid;
    }

    public String getBeadstype() {
        return beadstype;
    }

    public String getBias() {
        return bias;
    }

    public String getBlock() {
        return block;
    }

    public String getBuffer() {
        return buffer;
    }

    public String getCellcompartment() {
        return cellcompartment;
    }

    public String getCellconcentration() {
        return cellconcentration;
    }

    public String getCellline() {
        return cellline;
    }

    public String getCellnumbers() {
        return cellnumbers;
    }

    public String getCellsize() {
        return cellsize;
    }

    public String getCelltype() {
        return celltype;
    }

    public String getCellviability() {
        return cellviability;
    }

    public List<String> getChemicalmodifications() {
        return chemicalmodifications;
    }

    public String getCompoundclass() {
        return compoundclass;
    }

    public String getConcentration() {
        return concentration;
    }

    public String getConcentrationinputqc() {
        return concentrationinputqc;
    }

    public String getConcentrationinrange() {
        return concentrationinrange;
    }

    public String getConcentrationloading() {
        return concentrationloading;
    }

    public String getConcentrationmolar() {
        return concentrationmolar;
    }

    public String getConcentrationmolarinrange() {
        return concentrationmolarinrange;
    }

    public String getConcentrationprotein() {
        return concentrationprotein;
    }

    public String getCondition() {
        return condition;
    }

    public String getContainerid() {
        return containerid;
    }

    public String getCorrectionrate() {
        return correctionrate;
    }

    public String getCoverage() {
        return coverage;
    }

    public String getCq() {
        return cq;
    }

    public String getCrisprlibrary() {
        return crisprlibrary;
    }

    public String getDevelopmentstage() {
        return developmentstage;
    }

    public String getDigestionprotocol() {
        return digestionprotocol;
    }

    public String getDilution() {
        return dilution;
    }

    public String getDiseasestate() {
        return diseasestate;
    }

    public String getDmxflag() {
        return dmxflag;
    }

    public String getDsodn() {
        return dsodn;
    }

    public String getDv200() {
        return dv200;
    }

    public String getEffectortype() {
        return effectortype;
    }

    public String getEmbeddingmedium() {
        return embeddingmedium;
    }

    public List<String> getEnzymes() {
        return enzymes;
    }

    public String getExpressionsystem() {
        return expressionsystem;
    }

    public String getExtractionprotocol() {
        return extractionprotocol;
    }

    public String getExtractionprotocolstring() {
        return extractionprotocolstring;
    }

    public String getFastqscreen() {
        return fastqscreen;
    }

    public String getFixation() {
        return fixation;
    }

    public String getFraction() {
        return fraction;
    }

    public String getGeneticmodification() {
        return geneticmodification;
    }

    public String getGenomiccoordinates() {
        return genomiccoordinates;
    }

    public String getGenotype() {
        return genotype;
    }

    public String getGroupingvar() {
        return groupingvar;
    }

    public String getGrowthconditions() {
        return growthconditions;
    }

    public String getGuidename() {
        return guidename;
    }

    public String getGuidesequence() {
        return guidesequence;
    }

    public String getHybridizationprotocol() {
        return hybridizationprotocol;
    }

    public String getImmunoprecipitationtarget() {
        return immunoprecipitationtarget;
    }

    public String getIndividualid() {
        return individualid;
    }

    public String getInitialtimepoint() {
        return initialtimepoint;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getInstrumentid() {
        return instrumentid;
    }

    public String getInstrumentmethod() {
        return instrumentmethod;
    }

    public String getIntegritynumber() {
        return integritynumber;
    }

    public List<String> getInternalstandards() {
        return internalstandards;
    }

    public String getIrts() {
        return irts;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelamount() {
        return labelamount;
    }

    public String getLabelingmethod() {
        return labelingmethod;
    }

    public String getLabelingprotocol() {
        return labelingprotocol;
    }

    public String getLibraryprotocol() {
        return libraryprotocol;
    }

    public String getLibraryselection() {
        return libraryselection;
    }

    public String getLibrarystatus() {
        return librarystatus;
    }

    public String getLibrarystrategy() {
        return librarystrategy;
    }

    public String getLotinformation() {
        return lotinformation;
    }

    public String getLysisbuffer() {
        return lysisbuffer;
    }

    public String getMatrix() {
        return matrix;
    }

    public String getMedia() {
        return media;
    }

    public String getMolarity() {
        return molarity;
    }

    public String getMolarityfmol() {
        return molarityfmol;
    }

    public String getMolaritytarget() {
        return molaritytarget;
    }

    public String getMolecularweight() {
        return molecularweight;
    }

    public String getMultiplexed() {
        return multiplexed;
    }

    public String getMultiplexid() {
        return multiplexid;
    }

    public String getMultiplexid2() {
        return multiplexid2;
    }

    public String getMultiplexid2dmx() {
        return multiplexid2dmx;
    }

    public String getMultiplexiddmx() {
        return multiplexiddmx;
    }

    public String getMultiplexkit() {
        return multiplexkit;
    }

    public String getMultiplexkit2() {
        return multiplexkit2;
    }

    public String getMultiplexkit2id() {
        return multiplexkit2id;
    }

    public String getMultiplexkitid() {
        return multiplexkitid;
    }

    public String getNumberofcellsloaded() {
        return numberofcellsloaded;
    }

    public String getNumberofcycles() {
        return numberofcycles;
    }

    public String getOnslidemodification() {
        return onslidemodification;
    }

    public String getOrganism() {
        return organism;
    }

    public String getOrganismpart() {
        return organismpart;
    }

    public List<String> getParentid() {
        return parentid;
    }

    public String getPretreatment() {
        return pretreatment;
    }

    public String getProteinamount() {
        return proteinamount;
    }

    public String getPuritya260230() {
        return puritya260230;
    }

    public String getPuritya260280() {
        return puritya260280;
    }

    public String getQcpassed() {
        return qcpassed;
    }

    public String getQpcr() {
        return qpcr;
    }

    public String getQualitycontroltype() {
        return qualitycontroltype;
    }

    public String getQubit() {
        return qubit;
    }

    public String getReadcount() {
        return readcount;
    }

    public String getReadcounttotal() {
        return readcounttotal;
    }

    public String getRemultiplex() {
        return remultiplex;
    }

    public String getRemultiplexed() {
        return remultiplexed;
    }

    public String getRin() {
        return rin;
    }

    public String getSampleform() {
        return sampleform;
    }

    public String getSamplepreparationprotocol() {
        return samplepreparationprotocol;
    }

    public String getSamplepreparationprotocolid() {
        return samplepreparationprotocolid;
    }

    public String getSamplingdate() {
        return samplingdate;
    }

    public String getScanningprotocol() {
        return scanningprotocol;
    }

    public List<String> getSeparationtechniques() {
        return separationtechniques;
    }

    public String getSequencingmethod() {
        return sequencingmethod;
    }

    public String getSequencingmode() {
        return sequencingmode;
    }

    public String getSequencingplatform() {
        return sequencingplatform;
    }

    public String getSequencingprimer() {
        return sequencingprimer;
    }

    public String getSex() {
        return sex;
    }

    public String getSize() {
        return size;
    }

    public String getSizeaverage() {
        return sizeaverage;
    }

    public String getSizegenomeestimated() {
        return sizegenomeestimated;
    }

    public String getSizerange() {
        return sizerange;
    }

    public String getSlidetype() {
        return slidetype;
    }

    public String getSourcetype() {
        return sourcetype;
    }

    public String getSpecies() {
        return species;
    }

    public String getStatus() {
        return status;
    }

    public String getStrain() {
        return strain;
    }

    public String getSubjectid() {
        return subjectid;
    }

    public String getSurface() {
        return surface;
    }

    public String getTissue() {
        return tissue;
    }

    public String getTotalamount() {
        return totalamount;
    }

    public String getTreatment() {
        return treatment;
    }

    public String getTs() {
        return ts;
    }

    public String getTubeid() {
        return tubeid;
    }

    public String getType() {
        return type;
    }

    public String getVector() {
        return vector;
    }

    public String getVolume() {
        return volume;
    }

    public String getVolumedilutionsample() {
        return volumedilutionsample;
    }

    public String getVolumedilutionwater() {
        return volumedilutionwater;
    }

    public String getVolumeeluted() {
        return volumeeluted;
    }

    public String getVolumeinput() {
        return volumeinput;
    }

    public String getVolumelysisbuffer() {
        return volumelysisbuffer;
    }

    public String getVolumemeasured() {
        return volumemeasured;
    }

    public String getVolumereaction() {
        return volumereaction;
    }

    public String getVolumetarget() {
        return volumetarget;
    }

    public String getVolumetoaddebt() {
        return volumetoaddebt;
    }

    public String getVolumetoaddsample() {
        return volumetoaddsample;
    }

    public String getYield() {
        return yield;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setAgeunit(String ageunit) {
        this.ageunit = ageunit;
    }

    public void setAmounteluted(String amounteluted) {
        this.amounteluted = amounteluted;
    }

    public void setAmountinput(String amountinput) {
        this.amountinput = amountinput;
    }

    public void setAmounttotal(String amounttotal) {
        this.amounttotal = amounttotal;
    }

    public void setAmpliconsequence(String ampliconsequence) {
        this.ampliconsequence = ampliconsequence;
    }

    public void setArraydesignname(String arraydesignname) {
        this.arraydesignname = arraydesignname;
    }

    public void setAsiascale(String asiascale) {
        this.asiascale = asiascale;
    }

    public void setAveragesizeinrange(String averagesizeinrange) {
        this.averagesizeinrange = averagesizeinrange;
    }

    public void setBaitid(String baitid) {
        this.baitid = baitid;
    }

    public void setBeadstype(String beadstype) {
        this.beadstype = beadstype;
    }

    public void setBias(String bias) {
        this.bias = bias;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    public void setCellcompartment(String cellcompartment) {
        this.cellcompartment = cellcompartment;
    }

    public void setCellconcentration(String cellconcentration) {
        this.cellconcentration = cellconcentration;
    }

    public void setCellline(String cellline) {
        this.cellline = cellline;
    }

    public void setCellnumbers(String cellnumbers) {
        this.cellnumbers = cellnumbers;
    }

    public void setCellsize(String cellsize) {
        this.cellsize = cellsize;
    }

    public void setCelltype(String celltype) {
        this.celltype = celltype;
    }

    public void setCellviability(String cellviability) {
        this.cellviability = cellviability;
    }

    public void setChemicalmodifications(List<String> chemicalmodifications) {
        this.chemicalmodifications = chemicalmodifications;
    }

    public void setCompoundclass(String compoundclass) {
        this.compoundclass = compoundclass;
    }

    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public void setConcentrationinputqc(String concentrationinputqc) {
        this.concentrationinputqc = concentrationinputqc;
    }

    public void setConcentrationinrange(String concentrationinrange) {
        this.concentrationinrange = concentrationinrange;
    }

    public void setConcentrationloading(String concentrationloading) {
        this.concentrationloading = concentrationloading;
    }

    public void setConcentrationmolar(String concentrationmolar) {
        this.concentrationmolar = concentrationmolar;
    }

    public void setConcentrationmolarinrange(String concentrationmolarinrange) {
        this.concentrationmolarinrange = concentrationmolarinrange;
    }

    public void setConcentrationprotein(String concentrationprotein) {
        this.concentrationprotein = concentrationprotein;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setContainerid(String containerid) {
        this.containerid = containerid;
    }

    public void setCorrectionrate(String correctionrate) {
        this.correctionrate = correctionrate;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public void setCq(String cq) {
        this.cq = cq;
    }

    public void setCrisprlibrary(String crisprlibrary) {
        this.crisprlibrary = crisprlibrary;
    }

    public void setDevelopmentstage(String developmentstage) {
        this.developmentstage = developmentstage;
    }

    public void setDigestionprotocol(String digestionprotocol) {
        this.digestionprotocol = digestionprotocol;
    }

    public void setDilution(String dilution) {
        this.dilution = dilution;
    }

    public void setDiseasestate(String diseasestate) {
        this.diseasestate = diseasestate;
    }

    public void setDmxflag(String dmxflag) {
        this.dmxflag = dmxflag;
    }

    public void setDsodn(String dsodn) {
        this.dsodn = dsodn;
    }

    public void setDv200(String dv200) {
        this.dv200 = dv200;
    }

    public void setEffectortype(String effectortype) {
        this.effectortype = effectortype;
    }

    public void setEmbeddingmedium(String embeddingmedium) {
        this.embeddingmedium = embeddingmedium;
    }

    public void setEnzymes(List<String> enzymes) {
        this.enzymes = enzymes;
    }

    public void setExpressionsystem(String expressionsystem) {
        this.expressionsystem = expressionsystem;
    }

    public void setExtractionprotocol(String extractionprotocol) {
        this.extractionprotocol = extractionprotocol;
    }

    public void setExtractionprotocolstring(String extractionprotocolstring) {
        this.extractionprotocolstring = extractionprotocolstring;
    }

    public void setFastqscreen(String fastqscreen) {
        this.fastqscreen = fastqscreen;
    }

    public void setFixation(String fixation) {
        this.fixation = fixation;
    }

    public void setFraction(String fraction) {
        this.fraction = fraction;
    }

    public void setGeneticmodification(String geneticmodification) {
        this.geneticmodification = geneticmodification;
    }

    public void setGenomiccoordinates(String genomiccoordinates) {
        this.genomiccoordinates = genomiccoordinates;
    }

    public void setGenotype(String genotype) {
        this.genotype = genotype;
    }

    public void setGroupingvar(String groupingvar) {
        this.groupingvar = groupingvar;
    }

    public void setGrowthconditions(String growthconditions) {
        this.growthconditions = growthconditions;
    }

    public void setGuidename(String guidename) {
        this.guidename = guidename;
    }

    public void setGuidesequence(String guidesequence) {
        this.guidesequence = guidesequence;
    }

    public void setHybridizationprotocol(String hybridizationprotocol) {
        this.hybridizationprotocol = hybridizationprotocol;
    }

    public void setImmunoprecipitationtarget(String immunoprecipitationtarget) {
        this.immunoprecipitationtarget = immunoprecipitationtarget;
    }

    public void setIndividualid(String individualid) {
        this.individualid = individualid;
    }

    public void setInitialtimepoint(String initialtimepoint) {
        this.initialtimepoint = initialtimepoint;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public void setInstrumentid(String instrumentid) {
        this.instrumentid = instrumentid;
    }

    public void setInstrumentmethod(String instrumentmethod) {
        this.instrumentmethod = instrumentmethod;
    }

    public void setIntegritynumber(String integritynumber) {
        this.integritynumber = integritynumber;
    }

    public void setInternalstandards(List<String> internalstandards) {
        this.internalstandards = internalstandards;
    }

    public void setIrts(String irts) {
        this.irts = irts;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLabelamount(String labelamount) {
        this.labelamount = labelamount;
    }

    public void setLabelingmethod(String labelingmethod) {
        this.labelingmethod = labelingmethod;
    }

    public void setLabelingprotocol(String labelingprotocol) {
        this.labelingprotocol = labelingprotocol;
    }

    public void setLibraryprotocol(String libraryprotocol) {
        this.libraryprotocol = libraryprotocol;
    }

    public void setLibraryselection(String libraryselection) {
        this.libraryselection = libraryselection;
    }

    public void setLibrarystatus(String librarystatus) {
        this.librarystatus = librarystatus;
    }

    public void setLibrarystrategy(String librarystrategy) {
        this.librarystrategy = librarystrategy;
    }

    public void setLotinformation(String lotinformation) {
        this.lotinformation = lotinformation;
    }

    public void setLysisbuffer(String lysisbuffer) {
        this.lysisbuffer = lysisbuffer;
    }

    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setMolarity(String molarity) {
        this.molarity = molarity;
    }

    public void setMolarityfmol(String molarityfmol) {
        this.molarityfmol = molarityfmol;
    }

    public void setMolaritytarget(String molaritytarget) {
        this.molaritytarget = molaritytarget;
    }

    public void setMolecularweight(String molecularweight) {
        this.molecularweight = molecularweight;
    }

    public void setMultiplexed(String multiplexed) {
        this.multiplexed = multiplexed;
    }

    public void setMultiplexid(String multiplexid) {
        this.multiplexid = multiplexid;
    }

    public void setMultiplexid2(String multiplexid2) {
        this.multiplexid2 = multiplexid2;
    }

    public void setMultiplexid2dmx(String multiplexid2dmx) {
        this.multiplexid2dmx = multiplexid2dmx;
    }

    public void setMultiplexiddmx(String multiplexiddmx) {
        this.multiplexiddmx = multiplexiddmx;
    }

    public void setMultiplexkit(String multiplexkit) {
        this.multiplexkit = multiplexkit;
    }

    public void setMultiplexkit2(String multiplexkit2) {
        this.multiplexkit2 = multiplexkit2;
    }

    public void setMultiplexkit2id(String multiplexkit2id) {
        this.multiplexkit2id = multiplexkit2id;
    }

    public void setMultiplexkitid(String multiplexkitid) {
        this.multiplexkitid = multiplexkitid;
    }

    public void setNumberofcellsloaded(String numberofcellsloaded) {
        this.numberofcellsloaded = numberofcellsloaded;
    }

    public void setNumberofcycles(String numberofcycles) {
        this.numberofcycles = numberofcycles;
    }

    public void setOnslidemodification(String onslidemodification) {
        this.onslidemodification = onslidemodification;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public void setOrganismpart(String organismpart) {
        this.organismpart = organismpart;
    }

    public void setParentid(List<String> parentid) {
        this.parentid = parentid;
    }

    public void setPretreatment(String pretreatment) {
        this.pretreatment = pretreatment;
    }

    public void setProteinamount(String proteinamount) {
        this.proteinamount = proteinamount;
    }

    public void setPuritya260230(String puritya260230) {
        this.puritya260230 = puritya260230;
    }

    public void setPuritya260280(String puritya260280) {
        this.puritya260280 = puritya260280;
    }

    public void setQcpassed(String qcpassed) {
        this.qcpassed = qcpassed;
    }

    public void setQpcr(String qpcr) {
        this.qpcr = qpcr;
    }

    public void setQualitycontroltype(String qualitycontroltype) {
        this.qualitycontroltype = qualitycontroltype;
    }

    public void setQubit(String qubit) {
        this.qubit = qubit;
    }

    public void setReadcount(String readcount) {
        this.readcount = readcount;
    }

    public void setReadcounttotal(String readcounttotal) {
        this.readcounttotal = readcounttotal;
    }

    public void setRemultiplex(String remultiplex) {
        this.remultiplex = remultiplex;
    }

    public void setRemultiplexed(String remultiplexed) {
        this.remultiplexed = remultiplexed;
    }

    public void setRin(String rin) {
        this.rin = rin;
    }

    public void setSampleform(String sampleform) {
        this.sampleform = sampleform;
    }

    public void setSamplepreparationprotocol(String samplepreparationprotocol) {
        this.samplepreparationprotocol = samplepreparationprotocol;
    }

    public void setSamplepreparationprotocolid(String samplepreparationprotocolid) {
        this.samplepreparationprotocolid = samplepreparationprotocolid;
    }

    public void setSamplingdate(String samplingdate) {
        this.samplingdate = samplingdate;
    }

    public void setScanningprotocol(String scanningprotocol) {
        this.scanningprotocol = scanningprotocol;
    }

    public void setSeparationtechniques(List<String> separationtechniques) {
        this.separationtechniques = separationtechniques;
    }

    public void setSequencingmethod(String sequencingmethod) {
        this.sequencingmethod = sequencingmethod;
    }

    public void setSequencingmode(String sequencingmode) {
        this.sequencingmode = sequencingmode;
    }

    public void setSequencingplatform(String sequencingplatform) {
        this.sequencingplatform = sequencingplatform;
    }

    public void setSequencingprimer(String sequencingprimer) {
        this.sequencingprimer = sequencingprimer;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setSizeaverage(String sizeaverage) {
        this.sizeaverage = sizeaverage;
    }

    public void setSizegenomeestimated(String sizegenomeestimated) {
        this.sizegenomeestimated = sizegenomeestimated;
    }

    public void setSizerange(String sizerange) {
        this.sizerange = sizerange;
    }

    public void setSlidetype(String slidetype) {
        this.slidetype = slidetype;
    }

    public void setSourcetype(String sourcetype) {
        this.sourcetype = sourcetype;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStrain(String strain) {
        this.strain = strain;
    }

    public void setSubjectid(String subjectid) {
        this.subjectid = subjectid;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public void setTissue(String tissue) {
        this.tissue = tissue;
    }

    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public void setTubeid(String tubeid) {
        this.tubeid = tubeid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVector(String vector) {
        this.vector = vector;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public void setVolumedilutionsample(String volumedilutionsample) {
        this.volumedilutionsample = volumedilutionsample;
    }

    public void setVolumedilutionwater(String volumedilutionwater) {
        this.volumedilutionwater = volumedilutionwater;
    }

    public void setVolumeeluted(String volumeeluted) {
        this.volumeeluted = volumeeluted;
    }

    public void setVolumeinput(String volumeinput) {
        this.volumeinput = volumeinput;
    }

    public void setVolumelysisbuffer(String volumelysisbuffer) {
        this.volumelysisbuffer = volumelysisbuffer;
    }

    public void setVolumemeasured(String volumemeasured) {
        this.volumemeasured = volumemeasured;
    }

    public void setVolumereaction(String volumereaction) {
        this.volumereaction = volumereaction;
    }

    public void setVolumetarget(String volumetarget) {
        this.volumetarget = volumetarget;
    }

    public void setVolumetoaddebt(String volumetoaddebt) {
        this.volumetoaddebt = volumetoaddebt;
    }

    public void setVolumetoaddsample(String volumetoaddsample) {
        this.volumetoaddsample = volumetoaddsample;
    }

    public void setYield(String yield) {
        this.yield = yield;
    }
}