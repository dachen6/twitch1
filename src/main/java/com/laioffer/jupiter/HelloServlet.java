package com.laioffer.jupiter;
import org.json.JSONObject;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import org.apache.commons.io.IOUtils;
import com.laioffer.jupiter.entity.response.Game;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet(name = "helloServlet", value = "/game")
public class HelloServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World!";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        Game.Builder builder = new Game.Builder();
        builder.setName("world of craft");
        builder.setDeveloper("bizz");
        builder.setreleaseTime("feb 11");
        builder.setWebsite("https:www.word");
        builder.setPrice(49.99);

        Game game = builder.build();
        response.getWriter().print(mapper.writeValueAsString(game));

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {   //read game inofrmation from request body
        JSONObject jsonRequest = new JSONObject(IOUtils.toString(request.getReader()));
        String name = jsonRequest.getString("name");
        String developer = jsonRequest.getString("developer");
        String releaseTime = jsonRequest.getString("release_time");
        String website = jsonRequest.getString("website");
        float price = jsonRequest.getFloat("price");

        //print game information to IDE console
        System.out.println(name);
        System.out.println(developer);
        System.out.println(releaseTime);
        System.out.println(website);
        System.out.println(price);

        //return status = oj as response body to the client

        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "ok");
        response.getWriter().println(jsonResponse);

    }


    public void destroy() {
    }
}