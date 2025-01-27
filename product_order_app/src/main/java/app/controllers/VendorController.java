package app.controllers;

import app.dto.VendorViewDto;
import controllerlib.exposed.BaseController;
import controllerlib.exposed.ControllerResult;
import controllerlib.exposed.annotations.FromRequestBody;
import controllerlib.exposed.annotations.HttpGet;
import controllerlib.exposed.annotations.HttpPost;
import controllerlib.exposed.annotations.RequiredQueryParam;
import app.dto.VendorResponseDto;
import app.entity.VendorEntity;
import app.exceptions.EntityNotFoundException;
import app.repositories.VendorRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.sql.SQLException;
import java.util.List;

public class VendorController extends BaseController {
    private final VendorRepository repository = new VendorRepository();
    private final ModelMapper modelMapper = new ModelMapper();

    @HttpGet
    public ControllerResult<List<VendorResponseDto>> get() {
        try {
            List<VendorEntity> entities = repository.get();
            List<VendorResponseDto> dtos = modelMapper.map(entities, new TypeToken<List<VendorEntity>>() {
            }.getType());
            return Ok(dtos);
        } catch (SQLException e) {
            return InternalServerError();
        }
    }

    @HttpGet
    public ControllerResult<VendorResponseDto> get(@RequiredQueryParam("id") int id) {
        try {
            VendorEntity entity = repository.get(id);
            VendorResponseDto dto = modelMapper.map(entity, VendorResponseDto.class);
            return Ok(dto);
        } catch (SQLException e) {
            return InternalServerError();
        } catch (EntityNotFoundException e) {
            return NotFound();
        }
    }

    @HttpPost
    public ControllerResult<String> post(
            @FromRequestBody(isGenericCollection = true,
                    collectionType = List.class,
                    elementType = VendorViewDto.class) List<VendorViewDto> viedDtos) {
        return Ok(viedDtos.get(0).getName());
    }
}
