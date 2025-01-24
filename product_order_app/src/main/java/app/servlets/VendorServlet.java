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
            if (idParam == null) {
                List<VendorEntity> entities = repository.get();
                List<VendorResponseDto> dtos = modelMapper.map(entities, new TypeToken<List<VendorEntity>>() {
                }.getType());
                String json = jsonMapper.writeValueAsString(dtos);
                writer.write(json);
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                int id = Integer.parseInt(idParam);
                VendorEntity entity = repository.get(id);
                VendorResponseDto dto = modelMapper.map(entity, VendorResponseDto.class);
                String json = jsonMapper.writeValueAsString(dto);
                writer.write(json);
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
