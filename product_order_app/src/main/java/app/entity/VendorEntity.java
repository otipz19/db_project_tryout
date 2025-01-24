package app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class VendorEntity {
    private int id;
    @NonNull
    private String name;
    private String address;
}
