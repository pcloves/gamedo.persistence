package org.gamedo.persistence.db;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Document("player")
public class ComponentDbComplex extends ComponentDbData
{
    private boolean booleanValue;
    private byte byteValue;
    private short shortValue;
    private int intValue;
    private long longValue;

    private Boolean booleanBoxedValue;
    private Byte byteBoxedValue;
    private Short shortBoxedValue;
    private Integer intBoxedValue;
    private Long longBoxedValue;

    private String stringValue;
    private Date dateValue;

    private Set<InnerData> innerDataSet;
    private Map<Long, InnerInnerData> longInnerInnerDataMap;

    @Data
    @Builder
    public static class InnerData
    {
        private boolean booleanValue;
        private byte byteValue;
        private short shortValue;
        private int intValue;
        private long longValue;

        private Boolean booleanBoxedValue;
        private Byte byteBoxedValue;
        private Short shortBoxedValue;
        private Integer intBoxedValue;
        private Long longBoxedValue;

        private String stringValue;
        private Date dateValue;

        private Set<InnerInnerData> innerInnerDataSet;
        private Map<Long, InnerInnerData> longInnerInnerDataMap;
    }

    @Data
    @Builder
    public static class InnerInnerData
    {
        private boolean booleanValue;
        private byte byteValue;
        private short shortValue;
        private int intValue;
        private long longValue;

        private Boolean booleanBoxedValue;
        private Byte byteBoxedValue;
        private Short shortBoxedValue;
        private Integer intBoxedValue;
        private Long longBoxedValue;

        private String stringValue;
        private Date dateValue;

        private Set<Boolean> booleanSet;
        private Set<Short> shortSet;
        private Set<Integer> integerSet;
        private Set<Long> longSet;

        private Set<String> stringSet;

        private Map<Long, String> longStringMap;
        private Map<Long, Long> longLongMap;
    }
}
