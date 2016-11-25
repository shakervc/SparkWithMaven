import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import lombok.Data;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static spark.Spark.*;

public class SMSSparkJavaServer {
    // Enables CORS on requests. This method is an initialization method and should be called once.
    // Adapted from https://sparktutorials.github.io/2016/05/01/cors.html
    private static void enableCORS() {
// Can I automatically determine the port number and set it? Currently I am doing this manually?
         before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "http://localhost:63343");
            response.type("application/json");
        });
    }

    @Data
    private static class Student {
        private String fname;
        private String lname;
        private int id;

        Student(String fname, String lname, int id) {
            this.fname = fname;
            this.lname = lname;
            this.id = id;
        }
        void setID(int id) {
            this.id = id;
        }
// Add a validator
//        public boolean isValid() {
//            return title != null && !title.isEmpty() && !categories.isEmpty();
//        }
    }

    public static void main(String[] args) {
        enableCORS();
        Gson gsonBuilder = new GsonBuilder().create();
        Gson gson = new Gson();

        // Read data from files into global variables
        List<Student> physics = readFile("C:\\out\\physics.json");
        List<Student> chemistry = readFile("C:\\out\\chemistry.json");
        List<Student> biology = readFile("C:\\out\\biology.json");

        get("/physics/", (req, res) -> {
                    res.status(200);
                    res.type("application/json");
            return gsonBuilder.toJsonTree(physics).getAsJsonArray();
         } );

        get("/chemistry/",  (req, res) -> {
            res.status(200);
            res.type("application/json");
            return gsonBuilder.toJsonTree(chemistry).getAsJsonArray();
        } );

        get("/biology/",  (req, res) -> {
            res.status(200);
            res.type("application/json");
            return gsonBuilder.toJsonTree(biology).getAsJsonArray();
        } );

        get("/hello", (req, res) -> "Hello World");

        options("/*/*", (request, response) -> {
            response.status(200);
            response.type("application/json");
            response.header("Access-Control-Allow-Methods", "PUT, DELETE, POST");
            response.header("Access-Control-Allow-Headers",
                    "Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, " +
                    "Access-Control-Request-Method, Access-Control-Request-Headers");
            response.header("Access-Control-Allow-Headers", "Content-Type");
            response.header("Access-Control-Allow-Credentials", "true");
            return "OK";
        });

        post("/physics/", (request, response) -> {
            response.status(200);
            Student student = gson.fromJson(request.body(), Student.class);
            // For right now, just store some id. I should remove the need to store id's.
            student.setID(100);
            physics.add(student);
            JsonArray jsonArray = gsonBuilder.toJsonTree(physics).getAsJsonArray();
            writeFile(gson, jsonArray, "C:\\out\\physics.json");
            return gsonBuilder.toJsonTree(physics).getAsJsonArray();
        });

        post("/chemistry/", (request, response) -> {
            response.status(200);
            Student student = gson.fromJson(request.body(), Student.class);
            student.setID(100);
            chemistry.add(student);
            JsonArray jsonArray = gsonBuilder.toJsonTree(chemistry).getAsJsonArray();
            writeFile(gson, jsonArray, "C:\\out\\chemistry.json");
            return gsonBuilder.toJsonTree(chemistry).getAsJsonArray();
        });

        post("/biology/", (request, response) -> {
            response.status(200);
            Student student = gson.fromJson(request.body(), Student.class);
            student.setID(100);
            biology.add(student);
            JsonArray jsonArray = gsonBuilder.toJsonTree(biology).getAsJsonArray();
            writeFile(gson, jsonArray, "C:\\out\\biology.json");
            return gsonBuilder.toJsonTree(biology).getAsJsonArray();
        });
//        http://codingbat.com/doc/java-string-indexof-parsing.html
        put("/physics/*", (request, response) -> {
            updateCurrentStudent(request.body(), physics);
            JsonArray jsonArray = gsonBuilder.toJsonTree(physics).getAsJsonArray();
            writeFile(gson, jsonArray, "C:\\out\\physics.json");
            return gsonBuilder.toJsonTree(physics).getAsJsonArray();
        });

        put("/chemistry/*", (request, response) -> {
            updateCurrentStudent(request.body(), chemistry);
            JsonArray jsonArray = gsonBuilder.toJsonTree(chemistry).getAsJsonArray();
            writeFile(gson, jsonArray, "C:\\out\\chemistry.json");
            return gsonBuilder.toJsonTree(chemistry).getAsJsonArray();
        });

        put("/biology/*", (request, response) -> {
            updateCurrentStudent(request.body(), biology);
            JsonArray jsonArray = gsonBuilder.toJsonTree(biology).getAsJsonArray();
            writeFile(gson, jsonArray, "C:\\out\\biology.json");
            return gsonBuilder.toJsonTree(biology).getAsJsonArray();
        });

        // Do I need the /1 etc. in the delete?
        delete("/physics/*", (request, response) -> {
            physics.remove( Integer.parseInt(request.body().replaceAll("\\D+","") ) );
            JsonArray jsonArray = gsonBuilder.toJsonTree(physics).getAsJsonArray();
            writeFile(gson, jsonArray, "C:\\out\\physics.json");
            return gsonBuilder.toJsonTree(physics).getAsJsonArray();
        });

        delete("/chemistry/*", (request, response) -> {
            chemistry.remove( Integer.parseInt(request.body().replaceAll("\\D+","") ) );
            JsonArray jsonArray = gsonBuilder.toJsonTree(chemistry).getAsJsonArray();
            writeFile(gson, jsonArray, "C:\\out\\chemistry.json");
            return gsonBuilder.toJsonTree(chemistry).getAsJsonArray();
        });

        delete("/biology/*", (request, response) -> {
            biology.remove( Integer.parseInt(request.body().replaceAll("\\D+","") ) );
            JsonArray jsonArray = gsonBuilder.toJsonTree(biology).getAsJsonArray();
            writeFile(gson, jsonArray, "C:\\out\\biology.json");
            return gsonBuilder.toJsonTree(biology).getAsJsonArray();
        });
    }
    private static void writeFile(Gson gson, JsonArray jsonArray, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(jsonArray, writer);
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    private static List<Student> readFile(String filename) {
        Gson gson = new Gson();
        List<Student> course = new ArrayList<>();
        try (FileReader reader = new FileReader(filename)) {
            Student[] myData = gson.fromJson( reader, Student[].class );
            Collections.addAll(course, myData);
        } catch (IOException e) {
            System.out.println(filename + " was not found.");
//            e.printStackTrace();
        }
        return course;
    }
    private static void updateCurrentStudent(String putData, List course){
    /*
         Assumes that the data of the PUT is in this format:
            {"newdata":{"fname":"Meet","lname":"Me"},"index":1}

         1. Obtain fname, lname, and index from putData
         2. Update course modifying the element for the current student with data obtained from putData
            and a dummy id (which will be removed in the future or will come from the front end).
     */

    // Obtain fname, lname, and index from putData
        String[] inParts;     // Contains the results of splitting by ":"
        String fname, lname;
        int indexToCourse;
        /*
            Splitting putData results in an array containing 5 elements -- store this in inParts.
            Element at index 2 of inParts contains the value of fname and some other junk. Split this and remove "'s from the first
            element of the resulting array.
            Element at index 3 of inParts contains the value of lname and some other junk. Split this and remove "'s and }from the
            first element of the resulting array.
            Element at index 4 of inParts contains the value of index of course
         */
        inParts = putData.split(":");
        fname = inParts[2].split(",")[0].replaceAll("\"", "");
        lname = inParts[3].split(",")[0].replaceAll("\"", "").replaceAll("}", "");
        indexToCourse = Integer.parseInt(inParts[4].replaceAll("\\D+",""));

    // Update course modifying the element for the student at the position index
        course.set( indexToCourse, new Student(fname, lname, 500) );
    }
}

/*
 CORRECT FORMAT FOR COURSE DATA:

 Array of objects.
 "[ {\"fname\": \"Mu\", \"lname\": \"Neeshwar\", \"id\": \"1\"}, {\"fname\": \"Ga\", \"lname\": \"Noo\", \"id\": \"2\"} ]"

 The above doesn't pass JSON validation in: http://www.freeformatter.com/json-validator.html but works with angular.

 Single quotes don't work instead of escape. JSON standard does NOT allow single quotes

 TO DO

 . Clean up
 . Database for persistence

NOTES

 . Leave whatever I am doing currently with the ID part of each student's data. I don't really need it. The
   one backend I used (recommended by Bob Lindman) needed it. Currently I don't need it. If necessaery, I can have the
   front end create it or the database create it. An id is needed. Otherwise things break. Is the front end doing this?
   Possibly. The id need not be unique.

   http://stackoverflow.com/questions/21393792/update-and-read-json-array-from-file-using-gson

   What do the three blue bars on the right indicate?

*/

