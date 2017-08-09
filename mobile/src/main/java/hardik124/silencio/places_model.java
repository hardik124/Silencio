package hardik124.silencio;

public class places_model {

    private String name,address,key;

    public places_model(String name, String address, String key) {
        this.name = name;
        this.address = address;
        this.key = key;
    }

    public places_model() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
