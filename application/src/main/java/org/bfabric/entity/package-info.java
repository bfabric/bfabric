@XmlJavaTypeAdapters(value = {
    @XmlJavaTypeAdapter(type = LocalDate.class, value = LocalDateAdapter.class),
    @XmlJavaTypeAdapter(type = LocalDateTime.class, value = LocalDateTimeAdapter.class),
    @XmlJavaTypeAdapter(type = LocalTime.class, value = LocalTimeAdapter.class)
})

package org.bfabric.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.bfabric.xml.adapter.LocalDateAdapter;
import org.bfabric.xml.adapter.LocalDateTimeAdapter;
import org.bfabric.xml.adapter.LocalTimeAdapter;