package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Address columns shared by buyers, warehouses, orders and shipments. */
@Embeddable
public class AddressEmbeddable {

    @Column(name = "street", length = 150)
    private String street;

    @Column(name = "city", length = 80)
    private String city;

    @Column(name = "department", length = 80)
    private String department;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    public AddressEmbeddable() {
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}