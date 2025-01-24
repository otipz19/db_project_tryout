package app.servlets;

import app.dto.VendorResponseDto;
import app.entity.VendorEntity;
import app.exceptions.EntityNotFoundException;
import app.repositories.VendorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/vendor")
public class VendorServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            VendorRepository repository = new VendorRepository();
            ModelMapper modelMapper = new ModelMapper();
            ObjectMapper jsonMapper = new ObjectMapper();
            PrintWriter writer = resp.getWriter();
            String idParam = req.getParameter("id");
            Object result;
            if (idParam != null) {
                int id = Integer.parseInt(idParam);
                result = get(id, repository, modelMapper);
            } else {
                result = get(repository, modelMapper);
            }
            String json = jsonMapper.writeValueAsString(result);
            writer.write(json);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private List<VendorResponseDto> get(VendorRepository repository, ModelMapper modelMapper) throws SQLException {
        List<VendorEntity> entities = repository.get();
        return modelMapper.map(entities, new TypeToken<List<VendorEntity>>() {
        }.getType());
    }

    private VendorResponseDto get(int id, VendorRepository repository, ModelMapper modelMapper) throws SQLException, EntityNotFoundException {
        VendorEntity entity = repository.get(id);
        return modelMapper.map(entity, VendorResponseDto.class);
    }
}
