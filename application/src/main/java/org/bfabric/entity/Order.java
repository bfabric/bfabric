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

package org.bfabric.entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.CommentService;
import org.bfabric.service.OrderAttributeService;
import org.bfabric.service.OrderService;
import org.bfabric.util.ColorHelper;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.omnifaces.util.Ajax;

@Entity
@DynamicUpdate
@XmlRootElement
@NamedQuery(name = "Order.findAllTransitiveByUser", query = "SELECT a FROM Order a WHERE EXISTS(SELECT membership.id FROM Membership membership WHERE membership.user.id = :userId AND (membership.container = a or membership.container = a.project) and membership.discriminator = :discriminator)")
@NamedQuery(name = "Order.findPendingOrdersByCreated", query = "SELECT a FROM Order a WHERE a.status = org.bfabric.enums.StatusEnum.PENDING and a.created = :creationDate")
@NamedQuery(name = "Order.findPendingOrdersToBeCanceled", query = "SELECT a FROM Order a WHERE a.status = org.bfabric.enums.StatusEnum.PENDING and a.created < :creationDate")
@NamedQuery(name = "Order.hasSameServiceSampleCombination", query = "SELECT distinct i1.order.id FROM OrderItem i1 JOIN OrderItem i2 ON (i1.order.id = :orderId and i1.order.id = i2.order.id and i1.id < i2.id and i1.service.id=i2.service.id and i1.sample.id=i2.sample.id)")
public class Order extends Container {

    private static final long serialVersionUID = 1;

    @Transient
    private final List<String> generatedRowStyleClassesCoupled = new ArrayList<>();

    @Transient
    private final List<String> generatedRowStyleColorsCoupled = new ArrayList<>();

    @Transient
    private final Set<Long> invalidPlates = new HashSet<>();

    @Transient
    private final Set<Offer> initialOffers = new HashSet<>();

    @Transient
    BigInteger userSamplesCount;

    @XmlElement
    private Boolean bioSafetyLevel2PrecautionsRequired;

    @Column(updatable = false, insertable = false)
    private long chargeableOrderItemCount;

    @Column(updatable = false, insertable = false)
    private long chargedOrderItemCount;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<OrderComment> comments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumableid")
    @XmlIDREF
    private Consumable consumable;

    @XmlElement
    private Boolean customOption;

    @XmlElement
    private Boolean customPrimer;

    @XmlElement
    private Boolean darkCycle;

    @XmlElement
    private Boolean dataProduced;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demultiplexingid")
    @XmlIDREF
    private Demultiplexing demultiplexing;

    @DecimalMin("0")
    @DecimalMax(value = "100")
    @Digits(integer = 3, fraction = 2)
    @XmlElement
    private BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(columnDefinition = "TEXT")
    @XmlElement
    private String fastaSequence;

    @Transient
    private String generatedRowStyleClassCoupledStyleSheet = Constants.EMPTY_STRING;

    @Transient
    private Boolean hasSameServiceSampleCombination;

    @DecimalMin("0")
    @DecimalMax("9999")
    @Digits(integer = 4, fraction = 2)
    @XmlElement
    private BigDecimal hoursRequested;

    @Size(max = 256)
    @XmlElement
    private String index1;

    @Size(max = 256)
    @XmlElement
    private String index2;

    @Min(0)
    @XmlElement
    private Integer insertSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentdatadeliveryid")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private InstrumentDataDelivery instrumentDataDelivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentdatapackageid")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private InstrumentDataPackage instrumentDataPackage;

    @Transient
    private Boolean isProcessPlatesAndSubmittable;

    @Size(max = 256)
    @XmlElement
    private String kitsUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libraryprotocolid")
    @XmlIDREF
    private SamplePreparationProtocol libraryProtocol;

    @ManyToMany
    @JoinTable(name = "orderLibraryProtocolOptionValue", joinColumns = @JoinColumn(name = "orderId"), inverseJoinColumns = @JoinColumn(name = "optionValueId"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private Set<OptionValue> libraryProtocolOptionValues = new HashSet<>();

    @Transient
    private List<Option> libraryProtocolOptions;

    @Column(length = 32)
    @Size(max = 32)
    @XmlElement
    private String mailTrackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multiplexKitid")
    @XmlIDREF
    private MultiplexKit multiplexKit;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<OrderNote> notes = new HashSet<>();

    @XmlElement
    private Boolean nuclei;

    @DecimalMin("0")
    @XmlElement
    private BigDecimal numberOfCellsNuclei;

    @Transient
    private BigDecimal numberOfCellsNucleiOld;

    @Min(0)
    @Max(9999)
    @XmlElement
    private Integer numberOfChips;

    @Min(0)
    @XmlElement
    private Integer numberOfCyclesRead1;

    @Min(0)
    @XmlElement
    private Integer numberOfCyclesRead2;

    @Min(0)
    @Max(9999)
    @XmlElement
    private Integer numberOfPlates;

    @Size(max = 64)
    @XmlElement
    private String numberOfReplicates;

    @Min(0)
    @Max(9999)
    @XmlElement
    private Integer numberOfRunsSequencing;

    @Min(0)
    @Max(9999)
    @XmlElement
    private Integer numberOfRunsTapeStation;

    @Min(0)
    @Max(9999)
    @XmlElement
    private Integer numberOfSamples;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerid")
    @XmlIDREF
    private Offer offer;

    @Transient
    private Boolean orderItemIdRendered;

    @Transient
    private Boolean orderItemTubeIdRendered;

    @OneToMany(mappedBy = "order")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OrderItem> orderItems = new HashSet<>();

    @Transient
    private Integer orderItemsSamplesSize;

    @Transient
    private Integer orderItemsSize;

    @DecimalMin("0")
    @DecimalMax(value = "100")
    @Digits(integer = 3, fraction = 2)
    @XmlElement
    private BigDecimal phiX;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean processesPlates = false;

    @Size(max = 256)
    @XmlElement
    private String read1;

    @Size(max = 256)
    @XmlElement
    private String read2;

    @XmlElement
    private Boolean requestBclFile;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<OrderResult> results = new HashSet<>();

    @Column(length = 128)
    @Size(max = 128)
    @XmlElement
    private String sampleRetention;

    @Transient
    private Boolean sampleTubeIdRendered;

    @XmlElement
    private Boolean samplesContainTransgenes;

    @Transient
    private SortedSet<Sample> samplesOnPlate;

    @Transient
    private Integer samplesSubmitted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequencingapplicationid")
    @XmlIDREF
    private SequencingApplication sequencingApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequencingapplicationindexlengthid")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    private SequencingApplicationIndexLength sequencingApplicationIndexLength;

    @DecimalMin("0")
    @DecimalMax("9999")
    @Digits(integer = 4, fraction = 2)
    @XmlElement
    private BigDecimal totalNumberOfInstrumentDataPackages;

    @DecimalMin("0")
    @DecimalMax("9999")
    @Digits(integer = 4, fraction = 2)
    @XmlElement
    private BigDecimal userBenchUsage;

    public Order() {
        setSendMail(true);
    }

    @Override
    public Set<Mail> changeStatus(StatusEnum statusEnum) {
        Set<Mail> mails = new HashSet<>();
        setCreateAndAddStatus(statusEnum);
        // Set the coach for the accepted
        if (StatusEnum.ACCEPTED.equals(statusEnum) && !getCurrentUser().equals(getCoach())) {
            setCoachChanged(getCoach() != null && !getCoach().equals(getCurrentUser()));
            setCoach(getCurrentUser());
        }
        // Set the bioinformatician if analysis has started.
        if (StatusEnum.ANALYZING.equals(statusEnum) && isReplaceBioinformatician() && !getCurrentUser().equals(getBioinformatician())) {
            setBioinformatician(getCurrentUser());
            setBioinformaticianChanged(true);
        }
        if (isCoachChanged() || isCoachBackupChanged() || isBioinformaticianChanged()) {
            // Mail to inform that coaching has changed.
            mails.add(createMail(MailTypeEnum.CONTAINER_COACH_CHANGED));
        }
        // Explicit mail sending.
        if (isSendMail()) {
            switch (statusEnum) {
            case SUBMITTED:
                mails.add(createMail(MailTypeEnum.CONTAINER_SUBMITTED));
                break;
            case ACCEPTED:
                mails.add(createMail(MailTypeEnum.CONTAINER_ACCEPTED));
                break;
            case ARRIVED:
                mails.add(createMail(MailTypeEnum.CONTAINER_ARRIVED));
                break;
            case CANCELED:
                mails.add(createMail(MailTypeEnum.CONTAINER_CANCELED));
                break;
            case FINISHED:
                mails.add(createMail(MailTypeEnum.CONTAINER_FINISHED));
                break;
            default:
                break;
            }
        }
        return mails;
    }

    public void customOptionChanged(ValueChangeEvent event) {
        if (event.getNewValue() != null && (Boolean) event.getNewValue()) {
            if (getCustomPrimer() == null) {
                setCustomPrimer(Boolean.FALSE);
            }
            if (getDarkCycle() == null) {
                setDarkCycle(Boolean.FALSE);
            }
            if (getRequestBclFile() == null) {
                setRequestBclFile(Boolean.FALSE);
            }
        }
    }

    public void demultiplexingChanged(ValueChangeEvent event) {
        setDemultiplexing((Demultiplexing) event.getNewValue());
    }

    @Override
    public Set<Project> getAssociatedContainers() {
        Set<Project> tmpAssociatedContainers = new HashSet<>();
        if (getProject() != null) {
            tmpAssociatedContainers.add(getProject());
        }
        return tmpAssociatedContainers;
    }

    public Boolean getBioSafetyLevel2PrecautionsRequired() {
        return bioSafetyLevel2PrecautionsRequired;
    }

    public long getChargeableOrderItemCount() {
        return chargeableOrderItemCount;
    }

    public long getChargedOrderItemCount() {
        return chargedOrderItemCount;
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.ORDER_COMMENT;
    }

    @Override
    public Set<OrderComment> getComments() {
        return comments;
    }

    @Override
    public Long getCommentsTotalSize() {
        if (commentsTotalSize == null) {
            commentsTotalSize = getCommentsTotalSize(CDI.current().select(CommentService.class).get().getAllOrderCommentsAndNotesAndResultsByOrder(this, true));
        }
        return commentsTotalSize;
    }

    public String getConfirmationPDFLink() {
        return getReportPDFLink("order-confirmation-form-fop");
    }

    public Consumable getConsumable() {
        return consumable;
    }

    public String getCoupledMessage() {
        return getOrderItemsSize() != null && getOrderItemsSamplesSize() != null && getOrderItemsSize() > getOrderItemsSamplesSize() ? Messages.get("coupledMessage")
            .replace("{0}", String.valueOf(getOrderItemsSamplesSize())) : null;
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

    public Boolean getDataProduced() {
        return dataProduced;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    public Demultiplexing getDemultiplexing() {
        return demultiplexing;
    }

    @Override
    public BigDecimal getDiscount() {
        return discount;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        for (OrderAttribute orderAttribute : getServiceType().getOrderAttributes()) {
            try {
                addEntityInfoItem(summary, orderAttribute.getName(), getFieldValue(Order.class.getDeclaredField(orderAttribute.getName())));
            } catch (Exception ignored) {
            }
        }
        return summary.toString();
    }

    public String getFastaSequence() {
        return fastaSequence;
    }

    private Object getFieldValue(Field field) throws IllegalAccessException, InvocationTargetException {
        Object value = field.get(this);
        Object entityInfoValue = value;
        if (value != null) {
            try {
                Method getNameMethod = value.getClass().getMethod("getName");
                entityInfoValue = getNameMethod.invoke(value);
            } catch (NoSuchMethodException e) {
                try {
                    Method getIdMethod = value.getClass().getMethod("getId");
                    entityInfoValue = getIdMethod.invoke(value);
                } catch (NoSuchMethodException ignored) {
                }
            }
        }
        return entityInfoValue;
    }

    public String getFullName() {
        return (getProject() != null ? "p" + getProject().getId() + " - " : Constants.EMPTY_STRING) + getId() + " - " + getName();
    }

    @Override
    public Long getFullSize(boolean includeOrderData) {
        if (fullSize == null) {
            fullSize = super.getFullSize(includeOrderData);
            // Order specific, i.e., results (notes are internal and hence the attachments will not be downloaded) and executed workflow steps.
            for (OrderResult orderResult : getResults()) {
                fullSize += orderResult.getSize();
            }
        }
        return fullSize;
    }

    public String getGeneratedRowStyleClassCoupledStyleSheet() {
        return generatedRowStyleClassCoupledStyleSheet;
    }

    public List<String> getGeneratedRowStyleClassesCoupled() {
        return generatedRowStyleClassesCoupled;
    }

    public List<String> getGeneratedRowStyleColorsCoupled() {
        return generatedRowStyleColorsCoupled;
    }

    public BigDecimal getHoursRequested() {
        return hoursRequested;
    }

    public String getIndex1() {
        return index1;
    }

    public String getIndex2() {
        return index2;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();
        content.add(IndexMapContentEnum.NAME, "o" + getId());
        if (getServiceType() != null) {
            content.add(IndexMapContentEnum.SERVICETYPE, getServiceType().getName());
            if (getServiceType().getServiceArea() != null) {
                content.add(IndexMapContentEnum.SERVICEAREA, getServiceType().getServiceArea().getName());
            }
        }
        if (getSampleType() != null) {
            content.add(IndexMapContentEnum.SAMPLETYPE, getSampleType().getName());
        }
        if (getProject() != null) {
            content.add(IndexMapContentEnum.PROJECTID, getProject().getId());
        }
        if (getOffers() != null && !getOffers().isEmpty()) {
            StringBuilder offerIds = new StringBuilder();
            for (Offer aOffer : getOffers()) {
                offerIds.append(aOffer.getIdString()).append(" ");
            }
            content.add(IndexMapContentEnum.OFFERID, offerIds.toString());
        }
        content.add(IndexMapContentEnum.ITEMS, (long) getOrderItems().size());
        content.add(IndexMapContentEnum.CHARGEABLEITEMS, getChargeableOrderItemCount());
        content.add(IndexMapContentEnum.CHARGEDITEMS, getChargedOrderItemCount());
        if (StringHelper.isNotEmpty(getSummary())) {
            content.add(IndexMapContentEnum.SUMMARY, getSummary());
        }
        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.ORDER;
    }

    public Set<Offer> getInitialOffers() {
        return initialOffers;
    }

    public Integer getInsertSize() {
        return insertSize;
    }

    public InstrumentDataDelivery getInstrumentDataDelivery() {
        return instrumentDataDelivery;
    }

    public InstrumentDataPackage getInstrumentDataPackage() {
        return instrumentDataPackage;
    }

    public Set<Long> getInvalidPlates() {
        return invalidPlates;
    }

    public String getKitsUsed() {
        return kitsUsed;
    }

    public SamplePreparationProtocol getLibraryProtocol() {
        return libraryProtocol;
    }

    @SuppressWarnings("unused")
    public List<Option> getLibraryProtocolOptionList() {
        List<Option> libraryProtocolOptionList = getLibraryProtocolOptionValues().stream().map(OptionValue::getOption).sorted(Comparator.comparing(Option::getName)).distinct()
            .collect(Collectors.toList());
        for (Option option : libraryProtocolOptionList) {
            option.initOptionValueHolders(getLibraryProtocolOptionValues());
        }
        return libraryProtocolOptionList;
    }

    @SuppressWarnings("unused")
    public List<OptionValue> getLibraryProtocolOptionValueList() {
        return getLibraryProtocolOptionValues().stream().sorted(Comparator.comparing(optionValue -> optionValue.getOption().getName())).distinct().collect(Collectors.toList());
    }

    public Set<OptionValue> getLibraryProtocolOptionValues() {
        return libraryProtocolOptionValues;
    }

    public List<Option> getLibraryProtocolOptions() {
        if (libraryProtocolOptions == null) {
            libraryProtocolOptions = new ArrayList<>();
            if (getLibraryProtocol() != null && getLibraryProtocol().getOptions() != null) {
                for (Option option : getLibraryProtocol().getOptions()) {
                    option.initOptionValueHolders(getLibraryProtocolOptionValues());
                    if (option.isEnabled() || getLibraryProtocolOptionValues().stream().anyMatch(optionValue -> optionValue.getOption().equals(option))) {
                        libraryProtocolOptions.add(option);
                    }
                }
            }
        }
        return libraryProtocolOptions;
    }

    public String getMailTrackingNumber() {
        return mailTrackingNumber;
    }

    public MultiplexKit getMultiplexKit() {
        return multiplexKit;
    }

    public List<StatusEnum> getNextStates() {
        List<StatusEnum> nextStates = new ArrayList<>();
        StatusEnum statusEnum = getStatus();
        if (StatusEnum.PENDING.equals(statusEnum) && (!isProcessesSamples() || !getOrderItems().isEmpty())) {
            nextStates.add(StatusEnum.SUBMITTED);
        }
        if ((hasCurrentUserRoleEnum(RoleEnum.ADMIN) || isManager()) && isAcceptRevisionPending()) {
            nextStates.add(StatusEnum.REVISIONACCEPTED);
        }
        if (hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER)) {
            switch (statusEnum) {
            case SUBMITTED:
                nextStates.add(StatusEnum.ACCEPTED);
                if (isProcessesSamples() && !hasBeenArrived()) {
                    nextStates.add(StatusEnum.ARRIVED);
                }
                if (!hasBeenAccepted() && !hasBeenRevised()) {
                    nextStates.add(StatusEnum.REVISED);
                }
                break;
            case ACCEPTED:
                if (isProcessesSamples()) {
                    if (hasBeenArrived()) {
                        nextStates.add(StatusEnum.PROCESSING);
                    } else {
                        nextStates.add(StatusEnum.ARRIVED);
                    }
                } else {
                    nextStates.add(StatusEnum.FINISHED);
                }
                break;
            case ARRIVED:
                if (hasBeenAccepted()) {
                    nextStates.add(StatusEnum.PROCESSING);
                } else {
                    nextStates.add(StatusEnum.ACCEPTED);
                }
                if (!hasBeenAccepted() && !hasBeenRevised()) {
                    nextStates.add(StatusEnum.REVISED);
                }
                break;
            case REVISIONACCEPTED:
                if (hasBeenAccepted()) {
                    nextStates.add(StatusEnum.PROCESSING);
                } else {
                    nextStates.add(StatusEnum.ACCEPTED);
                }
                break;
            case PROCESSING:
                nextStates.add(StatusEnum.PROCESSED);
                break;
            case PROCESSED:
                nextStates.add(StatusEnum.ANALYZING);
                if (isAllItemsCharged()) {
                    nextStates.add(StatusEnum.FINISHED);
                }
                break;
            case ANALYZING:
                nextStates.add(StatusEnum.ANALYZED);
                break;
            case ANALYZED:
                if (isAllItemsCharged()) {
                    nextStates.add(StatusEnum.FINISHED);
                }
                break;
            case FINISHED:
                if (hasCurrentUserRoleEnum(RoleEnum.BOOKINGMANAGER) && isAllItemsCharged() && isAllBillableChargesBooked() && !hasBookingsWithoutSAPNumber()) {
                    nextStates.add(StatusEnum.CLOSED);
                }
                break;
            case CLOSED:
                if (hasCurrentUserRoleEnum(RoleEnum.BOOKINGMANAGER) || hasCurrentUserRoleEnum(RoleEnum.SERVICEMANAGER)) {
                    nextStates.add(StatusEnum.REOPENED);
                }
                break;
            case REOPENED:
                if (hasCurrentUserRoleEnum(RoleEnum.ADMIN) || getStatusModifiedBy().equals(getCurrentUser()) && (hasCurrentUserRoleEnum(RoleEnum.BOOKINGMANAGER) || hasCurrentUserRoleEnum(RoleEnum.SERVICEMANAGER))) {
                    nextStates.add(StatusEnum.CLOSED);
                }
                break;
            default:
                break;
            }
        }
        return nextStates;
    }

    @Override
    public CommentDiscriminator getNoteDiscriminator() {
        return CommentDiscriminator.ORDER_NOTE;
    }

    public Set<OrderNote> getNotes() {
        return notes;
    }

    public Boolean getNuclei() {
        return nuclei;
    }

    public BigDecimal getNumberOfCellsNuclei() {
        return numberOfCellsNuclei;
    }

    public BigDecimal getNumberOfCellsNucleiOld() {
        return numberOfCellsNucleiOld;
    }

    public Integer getNumberOfChips() {
        return numberOfChips;
    }

    public Integer getNumberOfCyclesRead1() {
        return numberOfCyclesRead1;
    }

    public Integer getNumberOfCyclesRead2() {
        return numberOfCyclesRead2;
    }

    public Integer getNumberOfPlates() {
        return numberOfPlates;
    }

    public String getNumberOfReplicates() {
        return numberOfReplicates;
    }

    public Integer getNumberOfRunsSequencing() {
        return numberOfRunsSequencing;
    }

    public Integer getNumberOfRunsTapeStation() {
        return numberOfRunsTapeStation;
    }

    public Integer getNumberOfSamples() {
        return numberOfSamples;
    }

    public Offer getOffer() {
        return offer;
    }

    public String getOrderAttributeHint(String attributeName) {
        return getServiceType() != null && getServiceType().getOrderAttribute(attributeName) != null ? getServiceType().getOrderAttribute(attributeName).getHint() : null;
    }

    @Override
    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public Integer getOrderItemsSamplesSize() {
        return orderItemsSamplesSize;
    }

    public Integer getOrderItemsSize() {
        return orderItemsSize;
    }

    public BigDecimal getPhiX() {
        return phiX;
    }

    public Boolean getProcessPlatesAndSubmittable() {
        return isProcessPlatesAndSubmittable;
    }

    public String getRead1() {
        return read1;
    }

    public String getRead2() {
        return read2;
    }

    public Boolean getRequestBclFile() {
        return requestBclFile;
    }

    @Override
    public CommentDiscriminator getResultDiscriminator() {
        return CommentDiscriminator.ORDER_RESULT;
    }

    public String getResultPDFLink() {
        return getReportPDFLink("order-result-form-fop");
    }

    public Set<OrderResult> getResults() {
        return results;
    }

    public String getSampleRetention() {
        return sampleRetention;
    }

    public Boolean getSamplesContainTransgenes() {
        return samplesContainTransgenes;
    }

    public SortedSet<Sample> getSamplesOnPlate() {
        if (samplesOnPlate == null) {
            samplesOnPlate = new TreeSet<>();
            for (OrderItem orderItem : getOrderItems()) {
                samplesOnPlate.addAll(orderItem.getPlate().getSamples());
            }
        }
        return samplesOnPlate;
    }

    public Integer getSamplesSubmitted() {
        if (samplesSubmitted == null) {
            samplesSubmitted = 0;
            Set<Plate> samplePlates = new HashSet<>();
            for (OrderItem orderItem : getOrderItems()) {
                if (orderItem.getSample() != null) {
                    samplesSubmitted++;
                } else if (orderItem.getPlate() != null && !samplePlates.contains(orderItem.getPlate())) {
                    samplePlates.add(orderItem.getPlate());
                    samplesSubmitted += orderItem.getPlate().getSamples().size();
                }
            }
        }
        return samplesSubmitted;
    }

    public SequencingApplication getSequencingApplication() {
        return sequencingApplication;
    }

    public SequencingApplicationIndexLength getSequencingApplicationIndexLength() {
        return sequencingApplicationIndexLength;
    }

    public BigDecimal getTotalNumberOfInstrumentDataPackages() {
        return totalNumberOfInstrumentDataPackages;
    }

    public BigDecimal getUserBenchUsage() {
        return userBenchUsage;
    }

    public BigInteger getUserSamplesCount() {
        if (userSamplesCount == null) {
            userSamplesCount = CDI.current().select(OrderService.class).get().getUserSamplesCount(getId());
        }
        return userSamplesCount;
    }

    public boolean hasSameServiceSampleCombination() {
        if (hasSameServiceSampleCombination == null) {
            hasSameServiceSampleCombination = CDI.current().select(OrderService.class).get().hasSameServiceSampleCombination(getId());
        }
        return hasSameServiceSampleCombination;
    }

    @Override
    public boolean hasSampleReplacements() {
        return getOrderItems().stream().anyMatch(OrderItem::hasSampleReplacements);
    }

    @Override
    public boolean hasSampleReplacementsToBeSent() {
        return getOrderItems().stream().anyMatch(OrderItem::hasSampleReplacements);
    }

    public boolean hasSamplesNotOrderItemAssociated() {
        Set<Sample> samplesNotOrderItemAssociated = new HashSet<>(getSamples());
        samplesNotOrderItemAssociated.removeAll(getOrderItemSamples());
        return !samplesNotOrderItemAssociated.isEmpty();
    }

    public boolean hasSamplesRequiringSameDayProcessing() {
        if (getServiceType() != null && getServiceType().isRequiresSameDayProcessing()) {
            for (Sample sample : getSamples()) {
                if (sample.isRequiringSameDayProcessing()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSequencingApplicationReadyMadeLibraries() {
        return getSequencingApplication() != null && getSequencingApplication().getName().equals("Ready-made Libraries");
    }

    public boolean hasServiceTypeHighThroughputSequencing() {
        return getServiceType() != null && getServiceType().getName().equals(Constants.SERVICE_TYPE_NAME_HIGH_THROUGHPUT_SEQUENCING);
    }

    public boolean hasServiceTypeReadyMadeLibrariesSequencing() {
        return getServiceType() != null && getServiceType().getName().equals(Constants.SERVICE_TYPE_NAME_READY_MADE_LIBRARIES_SEQUENCING);
    }

    public boolean hasServiceTypeReadyMadeLibrariesSequencingAndSequencingApplicationCustomOther() {
        return hasServiceTypeReadyMadeLibrariesSequencing() && getSequencingApplication() != null && getSequencingApplication().getName().equals(Constants.SEQUENCING_APPLICATION_NAME_CUSTOM_OTHER);
    }

    public void indexSamples() {
        IndexHelper.indexEntity(this);
        IndexHelper.indexEntities(getSamples());
    }

    public void initializeRowStyleClassAndRowTitleCoupled() {
        initializeRowStyleClassAndRowTitleCoupled(getOrderItems(), false);
    }

    public void initializeRowStyleClassAndRowTitleCoupled(Collection<OrderItem> orderItemCollection, boolean skipGeneratingRowStyleClassCoupledStyleSheet) {
        if (isManaged() && !isProcessesPlates() && orderItemCollection != null && !orderItemCollection.isEmpty()) {
            if (!skipGeneratingRowStyleClassCoupledStyleSheet) {
                for (String color : ColorHelper.getDistinctColorsRgba()) {
                    getGeneratedRowStyleClassesCoupled().add(color.replace(" ", Constants.EMPTY_STRING).replaceAll("[,.()]", "_"));
                    getGeneratedRowStyleColorsCoupled().add("background-color: " + color + " !important;");
                }
            }
            LinkedHashMap<Sample, Set<OrderItem>> sampleOrderItems = new LinkedHashMap<>();
            int aOrderItemsSize = 0;
            int aOrderItemsSamplesSize = 0;
            for (OrderItem orderItem : orderItemCollection) {
                Sample sample = orderItem.getSample();
                if (sample != null && !sampleOrderItems.containsKey(sample)) {
                    sampleOrderItems.put(sample, new HashSet<>());
                    aOrderItemsSamplesSize++;
                }
                sampleOrderItems.get(sample).add(orderItem);
                aOrderItemsSize++;
            }
            setOrderItemsSize(aOrderItemsSize > 0 ? aOrderItemsSize : null);
            setOrderItemsSamplesSize(aOrderItemsSamplesSize > 0 ? aOrderItemsSamplesSize : null);

            int rowStyleClassCoupledCounter = 0;
            for (Map.Entry<Sample, Set<OrderItem>> entrySet : sampleOrderItems.entrySet()) {
                if (rowStyleClassCoupledCounter == getGeneratedRowStyleClassesCoupled().size()) {
                    break;
                }
                if (entrySet.getValue().size() > 1) {
                    for (OrderItem orderItem : entrySet.getValue()) {
                        orderItem.setRowStyleClassCoupled(getGeneratedRowStyleClassesCoupled().get(rowStyleClassCoupledCounter));
                        orderItem.setRowTitleCoupled(Messages.get("rowTitleCoupled").replace("{0}", isOrderItemTubeIdRendered() ? orderItem.getTubeId() : entrySet.getKey().getTubeId()));
                    }
                    rowStyleClassCoupledCounter++;
                }
            }
            if (!skipGeneratingRowStyleClassCoupledStyleSheet) {
                setGeneratedRowStyleClassCoupledStyleSheet(ColorHelper.generateRowStyleClassCoupledStyleSheet(getGeneratedRowStyleClassesCoupled(), getGeneratedRowStyleColorsCoupled()));
            }
        }
    }

    public void instrumentChanged(ValueChangeEvent event) {
        Instrument newInstrument = (Instrument) event.getNewValue();
        if (newInstrument == null || getInstrumentDataPackage() != null && getInstrumentDataPackage().getInstrument() != null && !getInstrumentDataPackage().getInstrument().equals(newInstrument)) {
            setInstrumentDataPackage(null);
            setTotalNumberOfInstrumentDataPackages(null);
            setInstrumentReadConfiguration(null);
        }
        if (newInstrument == null || getInstrumentDataDelivery() != null && getInstrumentDataDelivery().getInstrument() != null && !getInstrumentDataDelivery().getInstrument().equals(newInstrument)) {
            setInstrumentDataDelivery(null);
        }
    }

    public void internalChanged(ValueChangeEvent event) {
        Boolean newInternal = (Boolean) event.getNewValue();
        if (newInternal != null) {
            if (!newInternal && isInternal() && getServiceType() != null && getServiceType().isInternal()) {
                setServiceType(null);
                setInstrumentDataDelivery(null);
                setInstrumentDataPackage(null);
                setTotalNumberOfInstrumentDataPackages(null);
                setInstrumentReadConfiguration(null);
            }
            setInternal(newInternal);
        }
    }

    public boolean isAcceptable() {
        if (getNextStates() != null && getNextStates().contains(StatusEnum.ACCEPTED) && getServiceType() != null) {
            // An order is acceptable iff: The next state is 'accepted', the service type is not null, and if no order attribute has a 'I do not know' option selected.
            final String iDoNotKnow = "I do not know";
            if (getServiceType().isOrderAttribute("instrumentDataPackage") && getInstrumentDataPackage() != null && getInstrumentDataPackage().getName() != null && StringHelper
                .isNotEmpty(getInstrumentDataPackage().getName()) && getInstrumentDataPackage().getName().startsWith(iDoNotKnow)) {
                return false;
            }
            if (getServiceType().isOrderAttribute("instrumentOptional") && getInstrument() == null) {
                return false;
            }
            if (getServiceType().isOrderAttribute("instrumentReadConfiguration") && getInstrument() != null && !getInstrument().getReadConfigurations()
                .isEmpty() && getInstrumentReadConfiguration() != null && StringHelper.isNotEmpty(getInstrumentReadConfiguration().getName()) && getInstrumentReadConfiguration().getName()
                .startsWith(iDoNotKnow)) {
                return false;
            }
            return !getServiceType().isOrderAttribute("libraryProtocol") || getInstrument() == null && getSequencingApplication() == null || getLibraryProtocol() == null || !StringHelper
                .isNotEmpty(getLibraryProtocol().getName()) || !getLibraryProtocol().getName().startsWith(iDoNotKnow);
        }
        return false;
    }

    public boolean isAcceptableNextState() {
        return getNextStates() != null && getNextStates().contains(StatusEnum.ACCEPTED);
    }

    public boolean isAllItemsCharged() {
        if (isProcessesSamples()) {
            for (OrderItem orderItem : getOrderItems()) {
                if (orderItem.isChargeable() && orderItem.getCharges().isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return !getCharges().isEmpty();
    }

    @Override
    public boolean isBillingAddressUpdatable() {
        return isPending() && isIdentityRequester() || isRequesterUpdatable();
    }

    public boolean isCancelable() {
        return isPending() && isManager() || hasCurrentUserRoleEnum(getDefaultRequiredRole()) && !(isCanceled() || isFinished() || isClosed() || isReopened()) && getNonBookedCharges().isEmpty();
    }

    @Override
    public boolean isChargeable() {
        return super.isChargeable() && !isPending();
    }

    @Override
    public boolean isCoach(User user) {
        return user != null && (user.equals(getCoach()) || getProject() != null && user.equals(getProject().getCoach()));
    }

    @Override
    public boolean isCoachBackup(User user) {
        return user != null && (user.equals(getCoachBackup()) || getProject() != null && user.equals(getProject().getCoachBackup()));
    }

    @Override
    public boolean isComputerLoginEnabled() {
        return hasBeenAccepted();
    }

    public boolean isContactOrBudgetOfficer(User user) {
        return isContact(user) || isBudgetOfficer(user);
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDataAccessEnabled() {
        return hasBeenAccepted();
    }

    @Override
    public boolean isDeletable() {
        return (isPending() || isCanceled()) && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isManager()) && super.isDeletable();
    }

    public boolean isDiscountRateUpdatable() {
        return getBookings().isEmpty() && hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDownloadable() {
        return super.isDownloadable() && !getStatus().equals(StatusEnum.PENDING) && !getStatus().equals(StatusEnum.SUBMITTED) && !getStatus().equals(StatusEnum.REJECTED);
    }

    @Override
    public boolean isExtensible() {
        return !isClosed() && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isMember()) || hasBeenAccepted() && hasCurrentUserRoleEnum(RoleEnum.FEEDER);
    }

    public boolean isIdentityRequester() {
        return getRequester() != null && getRequester().isIdentityUser();
    }

    public boolean isInUpdatableState() {
        return !isCanceled() && !isClosed();
    }

    public boolean isLabelable() {
        return getServiceType() != null && getServiceType().getSampleType() != null && SampleTypeEnum.isLabelableBySampleTypeName(getServiceType().getSampleType()
            .getName()) && !getOrderItems().isEmpty();
    }

    @Override
    public boolean isManager(User user) {
        return getProject() != null && (getProject().isManager(user) || isBudgetOfficer(user) || isContact(user) || isRequester(user) && getProject().isMember(user)) || super.isManager(user);
    }

    public boolean isOrderAttributeConfirmationFormRendered(String attributeName) {
        return attributeName != null && !attributeName.equals("fastaSequence");
    }

    public boolean isOrderItemIdRendered() {
        if (orderItemIdRendered == null) {
            orderItemIdRendered = !isSampleTubeIdRendered() && getOldServiceOrderId() != null;
        }
        return orderItemIdRendered;
    }

    public boolean isOrderItemSampleEditable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && isInUpdatableState() && !getOrderItems().isEmpty();
    }

    public boolean isOrderItemTubeIdRendered() {
        if (orderItemTubeIdRendered == null) {
            orderItemTubeIdRendered = isProcessesPlates() || !isSampleTubeIdRendered() && getOldProjectOrderId() != null;
        }
        return orderItemTubeIdRendered;
    }

    public boolean isProcessesPlates() {
        return processesPlates;
    }

    public boolean isProcessesSamples() {
        return getServiceType() != null && getServiceType().isProcessesSamples();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || isContactOrBudgetOfficer(getCurrentUser()) || isMemberTransitive();
    }

    public boolean isRenderedAddAttachmentButton() {
        return isUpdatable();
    }

    public boolean isRenderedAddChargeButton() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && !isCanceled() && !isPending() && !isClosed();
    }

    public boolean isRenderedAddNoteButton() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isRenderedAddOrderItemButton() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && !isCanceled() && !isPending() && !isFinished() && !isClosed() || isPending();
    }

    public boolean isRenderedAddResultButton() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && !isCanceled() && !isPending();
    }

    @Override
    public boolean isRenderedAddWorkflowButton() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && isNotPendingOrSubmittedOrCanceled();
    }

    public boolean isRenderedBookAllBillableChargesHint() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && isFinished() && !isAllBillableChargesBooked();
    }

    public boolean isRenderedChargeAllItemsHint() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && (isAccepted() || isArrived() || isProcessed() || isAnalyzed() || isFinished()) && !getOrderItems()
            .isEmpty() && !isAllItemsCharged();
    }

    public boolean isRenderedChargeOrderHint() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && (isAccepted() || isProcessed() || isAnalyzed() || isFinished()) && !isProcessesSamples() && getCharges().isEmpty();
    }

    public boolean isRenderedDeleteOrderItems() {
        boolean render = false;
        if (isUpdatable()) {
            for (OrderItem orderItem : getOrderItems()) {
                if (orderItem.isDeletable()) {
                    render = true;
                    break;
                }
            }
        }
        return render;
    }

    public boolean isRenderedEditOrderItems() {
        boolean render = false;
        if (isUpdatable()) {
            for (OrderItem orderItem : getOrderItems()) {
                if (orderItem.isUpdatable()) {
                    render = true;
                    break;
                }
            }
        }
        return render;
    }

    public boolean isRenderedFeedbackButton() {
        return isNotPendingOrSubmitted() && super.isRenderedFeedbackButton() && getDefaultFeedbackTemplateId() != null;
    }

    public boolean isRenderedHasSameServiceSampleCombinationHint() {
        return isInUpdatableState() && hasSameServiceSampleCombination();
    }

    public boolean isRenderedInstructions() {
        return isSubmitted() || isAccepted();
    }

    public boolean isRenderedOrderCoach() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER);
    }

    public boolean isRenderedOrderCoachBackup() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || getCurrentUser().equals(getCoach());
    }

    public boolean isRenderedPrintOrderConfirmationButton() {
        return !getOrderItems().isEmpty() && (hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) || isIdentityRequester() || isMemberTransitive() && !(isPending() || isCanceled()));
    }

    public boolean isRenderedSamples() {
        return getSamples().size() > getOrderItemSamples().size() || isProcessesPlates() && !getOrderItems().isEmpty() || hasSamplesNotOrderItemAssociated();
    }

    public boolean isRenderedSendEmailCheckbox() {
        return getNextStates() != null && (getNextStates().contains(StatusEnum.ACCEPTED) || getNextStates().contains(StatusEnum.ARRIVED) || getNextStates().contains(StatusEnum.FINISHED));
    }

    public boolean isRequester(User user) {
        return getRequester().equals(user);
    }

    public boolean isRequesterOrBudgetOfficer(User user) {
        return isRequester(user) || isBudgetOfficer(user);
    }

    public boolean isRequesterUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isReusingSamplesFromAnotherContainer() {
        for (Sample sample : getOrderItemSamples()) {
            if (!sample.getContainer().equals(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isRollbackable() {
        if (isClosed()) {
            return hasCurrentUserRoleEnum(RoleEnum.BOOKINGMANAGER);
        }
        if (!isPending()) {
            if (hasCurrentUserRoleEnum(RoleEnum.FEEDER) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER)) {
                return true;
            }
            // The ability for users to roll back the order status as they are only allowed to rollback from submitted to pending.
            if (hasCurrentUserRoleEnum(RoleEnum.USER) && isSubmitted() && !(getLastState() != null && getLastState().isCustomContainerStatus())) {
                return isReadable();
            }
        }
        return false;
    }

    public boolean isSampleTubeIdRendered() {
        if (sampleTubeIdRendered == null) {
            sampleTubeIdRendered = !isProcessesPlates() && !isCreatedBeforeBfabric10ReleaseDate();
        }
        return sampleTubeIdRendered;
    }

    public boolean isSampleTypeSpecificAttributesEditable() {
        return !hasServiceTypeReadyMadeLibrariesSequencing() || hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE);
    }

    public boolean isSamplesRequiringSameDayProcessingHint() {
        return (isPending() || isSubmitted() || isAccepted()) && hasSamplesRequiringSameDayProcessing();
    }

    public boolean isServiceTypeUpdatable() {
        return isIdentityRequester() && isPending() || hasCurrentUserRoleEnum(getDefaultRequiredRole()) && (isPending() || isSubmitted()) || hasCurrentUserRoleEnum(RoleEnum.ADMIN);
    }

    public boolean isSubmitReplacementsSendHintRendered() {
        return isProcessing() && WAITING_FOR_REPLACEMENT_SAMPLES.equals(getCustomStatus()) && hasSampleReplacements();
    }

    public boolean isSubmittable() {
        return isPending() && (!getServiceType().isProcessesSamples() || !getOrderItems().isEmpty());
    }

    @Override
    public boolean isUpdatable() {
        // Order is updatable by employees unless it is not finished/closed/canceled. Order is updatable by the user if he is either the requester of the order or a member of the containing project.
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && isInUpdatableState() || isPending() && isIdentityRequester();
    }

    public boolean isValidFastaSequence() {
        return isValidFastaSequence(getFastaSequence());
    }

    public boolean isValidFastaSequence(String value) {
        if (StringHelper.isNotEmpty(value)) {
            Scanner sc = new Scanner(value);
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (!line.isEmpty() && line.charAt(0) != '>' && !line.matches("^[A-Z\\-\\*]*$")) {
                    return false;
                }
            }
        }
        return true;
    }

    public void libraryProtocolChanged(ValueChangeEvent event) {
        setLibraryProtocol((SamplePreparationProtocol) event.getNewValue());
        resetLibraryProtocolOptionsNull();
    }

    public void libraryProtocolOptionValueChanged(ValueChangeEvent event) {
        OptionValue newOptionValue = (OptionValue) event.getNewValue();
        if (newOptionValue != null) {
            getLibraryProtocolOptionValues().removeIf(optionValue -> optionValue.getOption().equals(newOptionValue.getOption()));
            getLibraryProtocolOptionValues().add(newOptionValue);
        } else {
            OptionValue oldOptionValue = (OptionValue) event.getOldValue();
            if (oldOptionValue != null) {
                getLibraryProtocolOptionValues().removeIf(optionValue -> optionValue.getOption().equals(oldOptionValue.getOption()));
            }
        }
    }

    public void libraryProtocolOptionValuesChanged(ValueChangeEvent event) {
        Set<OptionValue> newOptionValues = (Set<OptionValue>) event.getNewValue();
        if (newOptionValues != null && !newOptionValues.isEmpty()) {
            Option newOption = newOptionValues.stream().findFirst().get().getOption();
            getLibraryProtocolOptionValues().removeIf(oldOptionValue -> oldOptionValue.getOption().equals(newOption));
            getLibraryProtocolOptionValues().addAll(newOptionValues);
        } else {
            Set<OptionValue> oldOptionValues = (Set<OptionValue>) event.getOldValue();
            if (oldOptionValues != null && !oldOptionValues.isEmpty()) {
                Option oldOption = oldOptionValues.stream().findFirst().get().getOption();
                if (oldOption != null) {
                    getLibraryProtocolOptionValues().removeIf(optionValue -> optionValue.getOption().equals(oldOption));
                }
            }
        }
    }

    public void offerChanged(ValueChangeEvent event) {
        Offer oldOffer = (Offer) event.getOldValue();
        setOffer((Offer) event.getNewValue());
        if (getOffer() != null) {
            getOffers().add(getOffer());
        }
        if (oldOffer != null && getOffers().contains(oldOffer) && (!getInitialOffers().contains(oldOffer) || getOffer() == null)) {
            getOffers().remove(oldOffer);
        }
    }

    @Override
    protected void postPersist() {
        super.postPersist();
        setName();
    }

    @Override
    protected void prePersist() {
        super.prePersist();
        setName();
    }

    public void processesPlatesChanged(ValueChangeEvent event) {
        Boolean newValue = (Boolean) event.getNewValue();
        if (newValue != null) {
            if (newValue) {
                setNumberOfSamples(null);
                if (hasSequencingApplicationReadyMadeLibraries()) {
                    // The sequencing application 'Ready-made Libraries' is not available for plate submissions.
                    setSequencingApplication(null);
                    // Clear all sequencing application dependents.
                    setLibraryProtocol(null);
                    Ajax.update(Constants.EDIT + ":instrumentDependent");
                }
            } else {
                setNumberOfPlates(null);
            }
        }
    }

    private void resetCustomPrimerDependent() {
        setIndex1(null);
        setIndex2(null);
        setRead1(null);
        setRead2(null);
    }

    private void resetDarkCycleDependent() {
        setNumberOfCyclesRead1(null);
        setNumberOfCyclesRead2(null);
    }

    public void resetLibraryProtocol() {
        libraryProtocol = null;
        resetLibraryProtocolOptionsNull();
    }

    public void resetLibraryProtocolOptions() {
        if (getLibraryProtocol() != null && getLibraryProtocol().getOptions() != null) {
            Set<Option> validOptions = new HashSet<>(getLibraryProtocol().getOptions());
            getLibraryProtocolOptionValues().removeIf(optionValue -> !validOptions.contains(optionValue.getOption()));
        }
    }

    public void resetLibraryProtocolOptionsNull() {
        libraryProtocolOptions = null;
    }

    public boolean resetNumberOfSamplesPlates(boolean allowResetToZero) {
        if (isProcessesSamples() && getOrderItems() != null) {
            if (getNumberOfPlates() != null && getOrderItems().size() != getNumberOfPlates() && (!getOrderItems().isEmpty() || allowResetToZero)) {
                setNumberOfPlates(getOrderItems().size());
                return true;
            }
            if (getNumberOfSamples() != null && getOrderItems().size() != getNumberOfSamples() && (!getOrderItems().isEmpty() || allowResetToZero)) {
                setNumberOfSamples(getOrderItems().size());
                return true;
            }
        }
        return false;
    }

    public void resetOrderAttributes() {
        if (getOrderAttributes() != null) {
            Set<OrderAttribute> invalidOrderAttributes = new HashSet<>((List<OrderAttribute>) CDI.current().select(OrderAttributeService.class).get().getResultList());
            Set<OrderAttribute> validOrderAttributes = new HashSet<>(getOrderAttributes());
            Set<String> validOrderAttributeNames = validOrderAttributes.stream().map(OrderAttribute::getName).collect(Collectors.toSet());
            invalidOrderAttributes.removeAll(validOrderAttributes);
            for (OrderAttribute orderAttribute : invalidOrderAttributes) {
                // Skip either instrument or instrumentOption if the other is an order attribute as both map to order.instrument.
                if (!("instrument".equals(orderAttribute.getName()) && validOrderAttributeNames.contains("instrumentOptional") || "instrumentOptional"
                    .equals(orderAttribute.getName()) && validOrderAttributeNames.contains("instrument")) &&
                    !("fastaSequenceRequired".equals(orderAttribute.getName()) && validOrderAttributeNames.contains("fastaSequence") || "fastaSequence"
                        .equals(orderAttribute.getName()) && validOrderAttributeNames.contains("fastaSequenceRequired"))) {
                    try {
                        Field field = Order.class.getDeclaredField(orderAttribute.getName());
                        field.set(this, null);
                    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException |
                             SecurityException ignored) {
                    }
                }
            }

            // Reset the library protocol iff the service type is 'Ready-made Libraries Sequencing' and the sequencing application 'Custom / Other'?
            if (hasServiceTypeReadyMadeLibrariesSequencingAndSequencingApplicationCustomOther()) {
                setLibraryProtocol(null);
            }

            // Reset the custom option dependent attributes.
            if (getCustomOption() == null || !getCustomOption()) {
                setCustomPrimer(null);
                resetCustomPrimerDependent();
                setDarkCycle(null);
                resetDarkCycleDependent();
                setRequestBclFile(null);
            } else {
                if (getCustomPrimer() == null || !getCustomPrimer()) {
                    resetCustomPrimerDependent();
                }
                if (getDarkCycle() == null || !getDarkCycle()) {
                    resetDarkCycleDependent();
                }
            }
        }
    }

    public void sampleRetentionChanged(ValueChangeEvent event) {
        setSampleRetention((String) event.getNewValue());
    }

    public void sequencingApplicationChanged(ValueChangeEvent event) {
        setSequencingApplication((SequencingApplication) event.getNewValue());
        if (getLibraryProtocol() != null && !getLibraryProtocol().getSequencingApplications().contains(getSequencingApplication())) {
            resetLibraryProtocol();
        }
    }

    public void setBioSafetyLevel2PrecautionsRequired(Boolean bioSafetyLevel2PrecautionsRequired) {
        this.bioSafetyLevel2PrecautionsRequired = bioSafetyLevel2PrecautionsRequired;
    }

    public void setComments(Set<OrderComment> comments) {
        this.comments = comments;
    }

    public void setConsumable(Consumable consumable) {
        this.consumable = consumable;
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

    public void setDataProduced(Boolean dataProduced) {
        this.dataProduced = dataProduced;
    }

    public void setDemultiplexing(Demultiplexing demultiplexing) {
        this.demultiplexing = demultiplexing;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = NumberUtils.getDecimalScale2(discount);
    }

    public void setFastaSequence(String fastaSequence) {
        this.fastaSequence = StringHelper.formatText(fastaSequence);
    }

    public void setGeneratedRowStyleClassCoupledStyleSheet(String generatedRowStyleClassCoupledStyleSheet) {
        this.generatedRowStyleClassCoupledStyleSheet = generatedRowStyleClassCoupledStyleSheet;
    }

    public void setHoursRequested(BigDecimal hoursRequested) {
        this.hoursRequested = hoursRequested;
    }

    public void setIndex1(String index1) {
        this.index1 = index1;
    }

    public void setIndex2(String index2) {
        this.index2 = index2;
    }

    public void setInsertSize(Integer insertSize) {
        this.insertSize = insertSize;
    }

    public void setInstrumentDataDelivery(InstrumentDataDelivery instrumentDataDelivery) {
        this.instrumentDataDelivery = instrumentDataDelivery;
    }

    public void setInstrumentDataPackage(InstrumentDataPackage instrumentDataPackage) {
        this.instrumentDataPackage = instrumentDataPackage;
    }

    public void setKitsUsed(String kitsUsed) {
        this.kitsUsed = StringHelper.format(kitsUsed);
    }

    public void setLibraryProtocol(SamplePreparationProtocol libraryProtocol) {
        this.libraryProtocol = libraryProtocol;
    }

    public void setLibraryProtocolOptionValues(Set<OptionValue> libraryProtocolOptionValues) {
        this.libraryProtocolOptionValues = libraryProtocolOptionValues;
    }

    public void setMailTrackingNumber(String mailTrackingNumber) {
        this.mailTrackingNumber = mailTrackingNumber;
    }

    public void setMultiplexKit(MultiplexKit multiplexKit) {
        this.multiplexKit = multiplexKit;
    }

    public void setName() {
        StringBuilder nameBuilder = new StringBuilder();
        if (getRequester() != null && StringHelper.isNotEmpty(getRequester().getFirstLastName())) {
            nameBuilder.append(getRequester().getFirstLastName());
        }
        if (getCreated() != null) {
            if (nameBuilder.length() > 0) {
                nameBuilder.append(" ");
            }
            nameBuilder.append(getCreatedFormattedAsDateString());
        }
        setName(nameBuilder.toString());
    }

    public void setNotes(Set<OrderNote> notes) {
        this.notes = notes;
    }

    public void setNuclei(Boolean nuclei) {
        this.nuclei = nuclei;
    }

    public void setNumberOfCellsNuclei(BigDecimal numberOfCellsNuclei) {
        this.numberOfCellsNuclei = numberOfCellsNuclei;
    }

    public void setNumberOfCellsNucleiOld(BigDecimal numberOfCellsNucleiOld) {
        this.numberOfCellsNucleiOld = numberOfCellsNucleiOld;
    }

    public void setNumberOfChips(Integer numberOfChips) {
        this.numberOfChips = numberOfChips;
    }

    public void setNumberOfCyclesRead1(Integer numberOfCyclesRead1) {
        this.numberOfCyclesRead1 = numberOfCyclesRead1;
    }

    public void setNumberOfCyclesRead2(Integer numberOfCyclesRead2) {
        this.numberOfCyclesRead2 = numberOfCyclesRead2;
    }

    public void setNumberOfPlates(Integer numberOfPlates) {
        this.numberOfPlates = numberOfPlates;
    }

    public void setNumberOfReplicates(String numberOfReplicates) {
        this.numberOfReplicates = StringHelper.format(numberOfReplicates);
    }

    public void setNumberOfRunsSequencing(Integer numberOfRunsSequencing) {
        this.numberOfRunsSequencing = numberOfRunsSequencing;
    }

    public void setNumberOfRunsTapeStation(Integer numberOfRunsTapeStation) {
        this.numberOfRunsTapeStation = numberOfRunsTapeStation;
    }

    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setOrderItemsSamplesSize(Integer orderItemsSamplesSize) {
        this.orderItemsSamplesSize = orderItemsSamplesSize;
    }

    public void setOrderItemsSize(Integer orderItemsSize) {
        this.orderItemsSize = orderItemsSize;
    }

    public void setPhiX(BigDecimal phiX) {
        this.phiX = phiX;
    }

    public void setProcessPlatesAndSubmittable(Boolean processPlatesAndSubmittable) {
        isProcessPlatesAndSubmittable = processPlatesAndSubmittable;
    }

    public void setProcessesPlates(boolean processesPlates) {
        this.processesPlates = processesPlates;
    }

    public void setRead1(String read1) {
        this.read1 = read1;
    }

    public void setRead2(String read2) {
        this.read2 = read2;
    }

    public void setRequestBclFile(Boolean requestBclFile) {
        this.requestBclFile = requestBclFile;
    }

    public void setResults(Set<OrderResult> results) {
        this.results = results;
    }

    public void setSampleRetention(String sampleRetention) {
        this.sampleRetention = sampleRetention;
    }

    public void setSamplesContainTransgenes(Boolean samplesContainTransgenes) {
        this.samplesContainTransgenes = samplesContainTransgenes;
    }

    public void setSequencingApplication(SequencingApplication sequencingApplication) {
        this.sequencingApplication = sequencingApplication;
    }

    public void setSequencingApplicationIndexLength(SequencingApplicationIndexLength sequencingApplicationIndexLength) {
        this.sequencingApplicationIndexLength = sequencingApplicationIndexLength;
    }

    public void setTotalNumberOfInstrumentDataPackages(BigDecimal totalNumberOfInstrumentDataPackages) {
        this.totalNumberOfInstrumentDataPackages = totalNumberOfInstrumentDataPackages;
    }

    public void setUserBenchUsage(BigDecimal userBenchUsage) {
        this.userBenchUsage = userBenchUsage;
    }

    public Set<Offer> updateOffers() {
        Set<Offer> updatedOffers = new HashSet<>();
        if (getInitialOffers() != null) {
            for (Offer aOffer : getOffers()) {
                if (!getInitialOffers().contains(aOffer)) {
                    updatedOffers.add(aOffer);
                }
            }
            for (Offer aOffer : getInitialOffers()) {
                if (!getOffers().contains(aOffer)) {
                    updatedOffers.add(aOffer);
                }
            }
        }
        return updatedOffers;
    }

    public boolean validateFastaSequence(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        String fastaSequence = (String) value;
        if (StringHelper.isNotEmpty(fastaSequence) && !isValidFastaSequence(fastaSequence)) {
            throw new BfabricValidatorException("fastaSequenceValidException");
        }
        return true;
    }

    public boolean validateFastaSequenceRequired(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        String fastaSequence = (String) value;
        if (StringHelper.isEmpty(fastaSequence) || !isValidFastaSequence(fastaSequence)) {
            throw new BfabricValidatorException("fastaSequenceValidException");
        }
        return true;
    }
}