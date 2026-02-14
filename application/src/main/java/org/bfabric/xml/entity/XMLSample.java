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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.beanutils.PropertyUtils;
import org.bfabric.entity.Annotation;
import org.bfabric.entity.CustomAttribute;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.MultiplexKit;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Run;
import org.bfabric.entity.RunUnitLane;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePlatePosition;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.SampleFormEnum;
import org.bfabric.enums.SampleQCTypeEnum;
import org.bfabric.enums.SampleStatusEnum;
import org.bfabric.util.StringHelper;

@XmlRootElement(name = "sample")
public class XMLSample extends XMLContainerReferencingEntity {

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
    private XMLAnnotation asiascale;

    @XmlElement
    private String averagesizeinrange;

    @XmlElement
    private String baitid;

    @XmlElement
    private String beadstype;

    @XmlElement
    private String bias;

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
    private List<XMLAnnotation> chemicalmodifications = new ArrayList<>();

    @XmlElement
    private List<XMLSample> child = new ArrayList<>();

    @XmlElement
    private XMLAnnotation compoundclass;

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
    private XMLAnnotation dsodn;

    @XmlElement
    private String dv200;

    @XmlElement
    private XMLAnnotation effectortype;

    @XmlElement
    private XMLAnnotation embeddingmedium;

    @XmlElement(name = "enzyme")
    private List<XMLAnnotation> enzymes = new ArrayList<>();

    @XmlElement
    private XMLAnnotation expressionsystem;

    @XmlElement
    private XMLAnnotation extractionprotocol;

    @XmlElement
    private String extractionprotocolstring;

    @XmlElement
    private String familycount;

    @XmlElement
    private String familyid;

    @XmlElement
    private String fastqscreen;

    @XmlElement
    private XMLAnnotation fixation;

    @XmlElement
    private String fraction;

    @XmlElement
    private String geneticmodification;

    @XmlElement
    private String genomiccoordinates;

    @XmlElement
    private String genotype;

    @XmlAttribute
    private String gridposition;

    @XmlElement
    private XMLAnnotation groupingvar;

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
    private XMLAnnotation initialtimepoint;

    @XmlElement
    private String instrument;

    @XmlElement
    private String instrumentid;

    @XmlElement
    private String instrumentmethod;

    @XmlElement
    private String integritynumber;

    @XmlElement(name = "internalstandard")
    private List<XMLAnnotation> internalstandards = new ArrayList<>();

    @XmlElement
    private String irts;

    @XmlElement
    private String label;

    @XmlElement
    private String labelamount;

    @XmlElement
    private XMLAnnotation labelingmethod;

    @XmlElement
    private String labelingprotocol;

    @XmlElement
    private String libraryprotocol;

    @XmlElement
    private String libraryselection;

    @XmlElement
    private String librarystrategy;

    @XmlElement
    private String lotinformation;

    @XmlElement
    private String lysisbuffer;

    @XmlElement
    private XMLAnnotation matrix;

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
    private String numberofcellsloaded;

    @XmlElement
    private String numberofcycles;

    @XmlElement
    private XMLAnnotation onslidemodification;

    @XmlElement
    private String organism;

    @XmlElement
    private XMLAnnotation organismpart;

    @XmlElement
    private List<XMLSample> parent = new ArrayList<>();

    @XmlElement
    private List<XMLPlate> plate = new ArrayList<>();

    @XmlAttribute
    private String position;

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
    private List<XMLCustomAttribute> qualitycontrolvalue = new ArrayList<>();

    @XmlElement
    private String qubit;

    @XmlElement
    private String readcount;

    @XmlElement
    private String readcounttotal;

    @XmlElement
    private String remultiplexed;

    @XmlElement
    private List<XMLSample> replacement = new ArrayList<>();

    @XmlElement
    private XMLSample replaces;

    @XmlElement
    private List<XMLResource> resource = new ArrayList<>();

    @XmlElement
    private String rin;

    @XmlElement
    private List<XMLRun> run = new ArrayList<>();

    @XmlElement
    private List<XMLRunUnitLane> rununitlane = new ArrayList<>();

    @XmlElement
    private String sampleform;

    @XmlElement
    private List<XMLSamplePlatePosition> sampleplateposition = new ArrayList<>();

    @XmlElement
    private String samplepreparationprotocol;

    @XmlElement
    private String samplepreparationprotocolid;

    @XmlElement
    private String samplingdate;

    @XmlElement
    private String scanningprotocol;

    @XmlElement(name = "separationtechnique")
    private List<XMLAnnotation> separationtechniques = new ArrayList<>();

    @XmlElement
    private XMLAnnotation sequencingmethod;

    @XmlElement
    private XMLAnnotation sequencingmode;

    @XmlElement
    private String sequencingplatform;

    @XmlElement
    private XMLAnnotation sequencingprimer;

    @XmlElement
    private XMLAnnotation sex;

    @XmlElement
    private String size;

    @XmlElement
    private String sizeaverage;

    @XmlElement
    private String sizegenomeestimated;

    @XmlElement
    private String sizerange;

    @XmlElement
    private XMLAnnotation slidetype;

    @XmlElement
    private XMLAnnotation sourcetype;

    @XmlElement
    private XMLAnnotation species;

    @XmlElement
    private String status;

    @XmlElement
    private String strain;

    @XmlElement
    private String subjectid;

    @XmlElement
    private XMLAnnotation surface;

    @XmlElement
    private String tissue;

    @XmlElement
    private String totalamount;

    @XmlElement
    private XMLAnnotation treatment;

    @XmlElement
    private String ts;

    @XmlElement
    private String tubeid;

    @XmlElement
    private String type;

    @XmlElement
    private String userdecision;

    @XmlElement
    private XMLAnnotation vector;

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

    public XMLSample() {
    }

    public XMLSample(Sample entity, boolean reference) {
        super(entity, reference);
    }

    public XMLSample(Sample entity) {
        super(entity);
        if (entity != null) {
            if (entity.getFamilyId() != null) {
                setFamilyid(String.valueOf(entity.getFamilyId()));
                setFamilycount(String.valueOf(entity.getFamily().size()));
            }
            if (entity.getType() != null) {
                setType(entity.getType());
            }
            if (entity.getReplaces() != null) {
                setReplaces(new XMLSample(entity.getReplaces(), true));
            }
            if (entity.getUserDecision() != null) {
                setUserdecision(entity.getUserDecision().name());
            }
            if (entity.getQualityControlValues() != null) {
                for (CustomAttribute qualityControlValue : entity.getQualityControlValues()) {
                    getQualitycontrolvalue().add(new XMLCustomAttribute(qualityControlValue));
                }
            }
            if (entity.getReadRequestParameter() != null) {
                if ((entity.getReadRequestParameter().includeassociations || entity.getReadRequestParameter().includereplacements) && entity.getReplacements() != null) {
                    for (Sample replacement : entity.getReplacements()) {
                        getReplacement().add(new XMLSample(replacement, true));
                    }
                }
                if ((entity.getReadRequestParameter().includeassociations || entity.getReadRequestParameter().includeparents) && entity.getParents() != null) {
                    for (Sample aParent : entity.getParents()) {
                        getParent().add(new XMLSample(aParent, true));
                    }
                }
                if ((entity.getReadRequestParameter().includeassociations || entity.getReadRequestParameter().includechildren) && entity.getChildren() != null) {
                    for (Sample aChild : entity.getChildren()) {
                        getChild().add(new XMLSample(aChild, true));
                    }
                }
                if ((entity.getReadRequestParameter().includeassociations || entity.getReadRequestParameter().includeresources) && entity.getResources() != null) {
                    for (Resource aResource : entity.getResources()) {
                        getResource().add(new XMLResource(aResource, true));
                    }
                }
                if (entity.getReadRequestParameter().includeassociations || entity.getReadRequestParameter().includeplates) {
                    if (entity.getPlates() != null) {
                        for (Plate plate : entity.getPlates()) {
                            getPlate().add(new XMLPlate(plate, true));
                        }
                    }
                    if (entity.getSamplePlatePositions() != null) {
                        for (SamplePlatePosition samplePlatePosition : entity.getSamplePlatePositions()) {
                            getSampleplateposition().add(new XMLSamplePlatePosition(samplePlatePosition, true));
                        }
                    }
                }
                if (entity.getReadRequestParameter().includeassociations || entity.getReadRequestParameter().includeruns) {
                    if (entity.getRunUnitLanes() != null) {
                        for (RunUnitLane runUnitLane : entity.getRunUnitLanes()) {
                            getRununitlane().add(new XMLRunUnitLane(runUnitLane, true));
                        }
                    }
                    if (entity.getRuns() != null) {
                        for (Run aRun : entity.getRuns()) {
                            getRun().add(new XMLRun(aRun, true));
                        }
                    }
                }
            }

            // Set all existent sample attributes.
            for (SampleAttributeEnum sampleAttributeEnum : SampleAttributeEnum.values()) {
                try {
                    String attributeNameLowerCase = sampleAttributeEnum.getName().toLowerCase();
                    Object value = PropertyUtils.getProperty(entity, sampleAttributeEnum.getName());
                    if (value != null) {

                        if (sampleAttributeEnum.isAnnotationTypeSingleValued()) {
                            PropertyUtils.setProperty(this, attributeNameLowerCase, new XMLAnnotation(((Annotation) value).getName(), ((Annotation) value).getId()));
                        } else if (sampleAttributeEnum.isAnnotationTypeMultiValued()) {
                            List<Annotation> annotations = (List<Annotation>) value;
                            if (!annotations.isEmpty()) {
                                List<XMLAnnotation> list = (List<XMLAnnotation>) PropertyUtils.getProperty(this, attributeNameLowerCase);
                                for (Annotation annotation : annotations) {
                                    list.add(new XMLAnnotation(annotation.getName(), annotation.getId()));
                                }
                            }
                        } else if (sampleAttributeEnum.isStringType()) {
                            if (StringHelper.isNotEmpty((String) value)) {
                                PropertyUtils.setProperty(this, attributeNameLowerCase, value);
                            }
                        } else if (sampleAttributeEnum.isNumericType()) {
                            PropertyUtils.setProperty(this, attributeNameLowerCase, String.valueOf(value));
                        } else if (sampleAttributeEnum.isLocalDateType()) {
                            PropertyUtils.setProperty(this, attributeNameLowerCase, String.valueOf(value));
                        } else if (sampleAttributeEnum.isLocalDateTimeType()) {
                            PropertyUtils.setProperty(this, attributeNameLowerCase, String.valueOf(value));
                        } else if (sampleAttributeEnum.isBooleanType()) {
                            PropertyUtils.setProperty(this, attributeNameLowerCase, String.valueOf(value));
                        } else if (sampleAttributeEnum.isSelectionAndNotAnnotationType() && !sampleAttributeEnum.isEnumType()) {
                            if (SampleAttributeEnum.SAMPLE_PREPARATION_PROTOCOL.equals(sampleAttributeEnum)) {
                                setSamplepreparationprotocol(((SamplePreparationProtocol) value).getName());
                            } else if (SampleAttributeEnum.MULTIPLEX_KIT.equals(sampleAttributeEnum)) {
                                setMultiplexkit(((MultiplexKit) value).getName());
                            } else if (SampleAttributeEnum.MULTIPLEX_KIT_2.equals(sampleAttributeEnum)) {
                                setMultiplexkit2(((MultiplexKit) value).getName());
                            } else if (SampleAttributeEnum.INSTRUMENT.equals(sampleAttributeEnum)) {
                                setInstrument(((Instrument) value).getName());
                            }
                        } else if (sampleAttributeEnum.isEnumType()) {
                            if (SampleFormEnum.class.equals(sampleAttributeEnum.getClazz())) {
                                PropertyUtils.setProperty(this, attributeNameLowerCase, ((SampleFormEnum) value).getLabel());
                            } else if (SampleQCTypeEnum.class.equals(sampleAttributeEnum.getClazz())) {
                                PropertyUtils.setProperty(this, attributeNameLowerCase, ((SampleQCTypeEnum) value).getLabel());
                            } else if (SampleStatusEnum.class.equals(sampleAttributeEnum.getClazz())) {
                                PropertyUtils.setProperty(this, attributeNameLowerCase, ((SampleStatusEnum) value).getLabel());
                            }
                        } else {
                            PropertyUtils.setProperty(this, attributeNameLowerCase, value);
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                }
            }
        }
    }

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

    public XMLAnnotation getAsiascale() {
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

    public List<XMLAnnotation> getChemicalmodifications() {
        return chemicalmodifications;
    }

    public List<XMLSample> getChild() {
        return child;
    }

    public XMLAnnotation getCompoundclass() {
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

    public XMLAnnotation getDsodn() {
        return dsodn;
    }

    public String getDv200() {
        return dv200;
    }

    public XMLAnnotation getEffectortype() {
        return effectortype;
    }

    public XMLAnnotation getEmbeddingmedium() {
        return embeddingmedium;
    }

    public List<XMLAnnotation> getEnzymes() {
        return enzymes;
    }

    public XMLAnnotation getExpressionsystem() {
        return expressionsystem;
    }

    public XMLAnnotation getExtractionprotocol() {
        return extractionprotocol;
    }

    public String getExtractionprotocolstring() {
        return extractionprotocolstring;
    }

    public String getFamilycount() {
        return familycount;
    }

    public String getFamilyid() {
        return familyid;
    }

    public String getFastqscreen() {
        return fastqscreen;
    }

    public XMLAnnotation getFixation() {
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

    public String getGridposition() {
        return gridposition;
    }

    public XMLAnnotation getGroupingvar() {
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

    public XMLAnnotation getInitialtimepoint() {
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

    public List<XMLAnnotation> getInternalstandards() {
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

    public XMLAnnotation getLabelingmethod() {
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

    public String getLibrarystrategy() {
        return librarystrategy;
    }

    public String getLotinformation() {
        return lotinformation;
    }

    public String getLysisbuffer() {
        return lysisbuffer;
    }

    public XMLAnnotation getMatrix() {
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

    public String getNumberofcellsloaded() {
        return numberofcellsloaded;
    }

    public String getNumberofcycles() {
        return numberofcycles;
    }

    public XMLAnnotation getOnslidemodification() {
        return onslidemodification;
    }

    public String getOrganism() {
        return organism;
    }

    public XMLAnnotation getOrganismpart() {
        return organismpart;
    }

    public List<XMLSample> getParent() {
        return parent;
    }

    public List<XMLPlate> getPlate() {
        return plate;
    }

    public String getPosition() {
        return position;
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

    public List<XMLCustomAttribute> getQualitycontrolvalue() {
        return qualitycontrolvalue;
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

    public String getRemultiplexed() {
        return remultiplexed;
    }

    public List<XMLSample> getReplacement() {
        return replacement;
    }

    public XMLSample getReplaces() {
        return replaces;
    }

    public List<XMLResource> getResource() {
        return resource;
    }

    public String getRin() {
        return rin;
    }

    public List<XMLRun> getRun() {
        return run;
    }

    public List<XMLRunUnitLane> getRununitlane() {
        return rununitlane;
    }

    public String getSampleform() {
        return sampleform;
    }

    public List<XMLSamplePlatePosition> getSampleplateposition() {
        return sampleplateposition;
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

    public List<XMLAnnotation> getSeparationtechniques() {
        return separationtechniques;
    }

    public XMLAnnotation getSequencingmethod() {
        return sequencingmethod;
    }

    public XMLAnnotation getSequencingmode() {
        return sequencingmode;
    }

    public String getSequencingplatform() {
        return sequencingplatform;
    }

    public XMLAnnotation getSequencingprimer() {
        return sequencingprimer;
    }

    public XMLAnnotation getSex() {
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

    public XMLAnnotation getSlidetype() {
        return slidetype;
    }

    public XMLAnnotation getSourcetype() {
        return sourcetype;
    }

    public XMLAnnotation getSpecies() {
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

    public XMLAnnotation getSurface() {
        return surface;
    }

    public String getTissue() {
        return tissue;
    }

    public String getTotalamount() {
        return totalamount;
    }

    public XMLAnnotation getTreatment() {
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

    public String getUserdecision() {
        return userdecision;
    }

    public XMLAnnotation getVector() {
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

    public void setAsiascale(XMLAnnotation asiascale) {
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

    public void setChemicalmodifications(List<XMLAnnotation> chemicalmodifications) {
        this.chemicalmodifications = chemicalmodifications;
    }

    public void setChild(List<XMLSample> child) {
        this.child = child;
    }

    public void setCompoundclass(XMLAnnotation compoundclass) {
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

    public void setDsodn(XMLAnnotation dsodn) {
        this.dsodn = dsodn;
    }

    public void setDv200(String dv200) {
        this.dv200 = dv200;
    }

    public void setEffectortype(XMLAnnotation effectortype) {
        this.effectortype = effectortype;
    }

    public void setEmbeddingmedium(XMLAnnotation embeddingmedium) {
        this.embeddingmedium = embeddingmedium;
    }

    public void setEnzymes(List<XMLAnnotation> enzymes) {
        this.enzymes = enzymes;
    }

    public void setExpressionsystem(XMLAnnotation expressionsystem) {
        this.expressionsystem = expressionsystem;
    }

    public void setExtractionprotocol(XMLAnnotation extractionprotocol) {
        this.extractionprotocol = extractionprotocol;
    }

    public void setExtractionprotocolstring(String extractionprotocolstring) {
        this.extractionprotocolstring = extractionprotocolstring;
    }

    public void setFamilycount(String familycount) {
        this.familycount = familycount;
    }

    public void setFamilyid(String familyid) {
        this.familyid = familyid;
    }

    public void setFastqscreen(String fastqscreen) {
        this.fastqscreen = fastqscreen;
    }

    public void setFixation(XMLAnnotation fixation) {
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

    public void setGridposition(String gridposition) {
        this.gridposition = gridposition;
    }

    public void setGroupingvar(XMLAnnotation groupingvar) {
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

    public void setInitialtimepoint(XMLAnnotation initialtimepoint) {
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

    public void setInternalstandards(List<XMLAnnotation> internalstandards) {
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

    public void setLabelingmethod(XMLAnnotation labelingmethod) {
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

    public void setLibrarystrategy(String librarystrategy) {
        this.librarystrategy = librarystrategy;
    }

    public void setLotinformation(String lotinformation) {
        this.lotinformation = lotinformation;
    }

    public void setLysisbuffer(String lysisbuffer) {
        this.lysisbuffer = lysisbuffer;
    }

    public void setMatrix(XMLAnnotation matrix) {
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

    public void setNumberofcellsloaded(String numberofcellsloaded) {
        this.numberofcellsloaded = numberofcellsloaded;
    }

    public void setNumberofcycles(String numberofcycles) {
        this.numberofcycles = numberofcycles;
    }

    public void setOnslidemodification(XMLAnnotation onslidemodification) {
        this.onslidemodification = onslidemodification;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public void setOrganismpart(XMLAnnotation organismpart) {
        this.organismpart = organismpart;
    }

    public void setParent(List<XMLSample> parent) {
        this.parent = parent;
    }

    public void setPlate(List<XMLPlate> plate) {
        this.plate = plate;
    }

    public void setPosition(String position) {
        this.position = position;
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

    public void setQualitycontrolvalue(List<XMLCustomAttribute> qualitycontrolvalue) {
        this.qualitycontrolvalue = qualitycontrolvalue;
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

    public void setRemultiplexed(String remultiplexed) {
        this.remultiplexed = remultiplexed;
    }

    public void setReplacement(List<XMLSample> replacement) {
        this.replacement = replacement;
    }

    public void setReplaces(XMLSample replaces) {
        this.replaces = replaces;
    }

    public void setResource(List<XMLResource> resource) {
        this.resource = resource;
    }

    public void setRin(String rin) {
        this.rin = rin;
    }

    public void setRun(List<XMLRun> run) {
        this.run = run;
    }

    public void setRununitlane(List<XMLRunUnitLane> rununitlane) {
        this.rununitlane = rununitlane;
    }

    public void setSampleform(String sampleform) {
        this.sampleform = sampleform;
    }

    public void setSampleplateposition(List<XMLSamplePlatePosition> sampleplateposition) {
        this.sampleplateposition = sampleplateposition;
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

    public void setSeparationtechniques(List<XMLAnnotation> separationtechniques) {
        this.separationtechniques = separationtechniques;
    }

    public void setSequencingmethod(XMLAnnotation sequencingmethod) {
        this.sequencingmethod = sequencingmethod;
    }

    public void setSequencingmode(XMLAnnotation sequencingmode) {
        this.sequencingmode = sequencingmode;
    }

    public void setSequencingplatform(String sequencingplatform) {
        this.sequencingplatform = sequencingplatform;
    }

    public void setSequencingprimer(XMLAnnotation sequencingprimer) {
        this.sequencingprimer = sequencingprimer;
    }

    public void setSex(XMLAnnotation sex) {
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

    public void setSlidetype(XMLAnnotation slidetype) {
        this.slidetype = slidetype;
    }

    public void setSourcetype(XMLAnnotation sourcetype) {
        this.sourcetype = sourcetype;
    }

    public void setSpecies(XMLAnnotation species) {
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

    public void setSurface(XMLAnnotation surface) {
        this.surface = surface;
    }

    public void setTissue(String tissue) {
        this.tissue = tissue;
    }

    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }

    public void setTreatment(XMLAnnotation treatment) {
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

    public void setUserdecision(String userdecision) {
        this.userdecision = userdecision;
    }

    public void setVector(XMLAnnotation vector) {
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