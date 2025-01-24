package app.controllers;

import controllerlib.ControllerResult;
import controllerlib.annotations.GetMethod;
import controllerlib.annotations.RequiredQueryParam;
import app.dto.VendorResponseDto;
import app.entity.VendorEntity;
import app.exceptions.EntityNotFoundException;
import app.repositories.VendorRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.sql.SQLException;
import java.util.List;

public class VendorController {
    private final VendorRepository repository = new VendorRepository();
    private final ModelMapper modelMapper = new ModelMapper();

    @GetMethod
    public ControllerResult<List<VendorResponseDto>> get() throws SQLException {
        try {
            List<VendorEntity> entities = repository.get();
            List<VendorResponseDto> dtos = modelMapper.map(entities, new TypeToken<List<VendorEntity>>() {
            }.getType());
            return new ControllerResult<>(dtos, HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            return new ControllerResult<>(null, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMethod
    public ControllerResult<VendorResponseDto> get(@RequiredQueryParam int id) {
        try {
            VendorEntity entity = repository.get(id);
            VendorResponseDto dto = modelMapper.map(entity, VendorResponseDto.class);
            return new ControllerResult<>(dto, HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            return new ControllerResult<>(null, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (EntityNotFoundException e) {
            return new ControllerResult<>(null, HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
