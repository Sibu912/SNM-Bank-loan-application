package io.github.sibu912.snmbank.loans.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * The person applying for a loan (page 1 of the form).
 */
@Entity
@Table(name = "applicant")
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "title", nullable = false, length = 10)
    private Title title;

    @Column(name = "given_name", nullable = false, length = 100)
    private String givenName;

    @Column(name = "surname", nullable = false, length = 100)
    private String surname;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "street", nullable = false, length = 150)
    private String street;

    @Column(name = "suburb", nullable = false, length = 100)
    private String suburb;

    @Column(name = "province", nullable = false, length = 50)
    private String province;

    @Column(name = "postal_code", nullable = false, length = 4)
    private String postalCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Required by JPA. */
    protected Applicant() {
    }

    public Applicant(Title title, String givenName, String surname, Gender gender,
                     String street, String suburb, String province, String postalCode) {
        this.title = title;
        this.givenName = givenName;
        this.surname = surname;
        this.gender = gender;
        this.street = street;
        this.suburb = suburb;
        this.province = province;
        this.postalCode = postalCode;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /** For example "Mr Thabo Nkosi". */
    public String getFullName() {
        return title.getDisplayName() + " " + givenName + " " + surname;
    }

    public Long getId() {
        return id;
    }

    public Title getTitle() {
        return title;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurname() {
        return surname;
    }

    public Gender getGender() {
        return gender;
    }

    public String getStreet() {
        return street;
    }

    public String getSuburb() {
        return suburb;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
