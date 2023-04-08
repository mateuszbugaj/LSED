package StreamingService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDatabase {
    private final String fileName;
    ObjectMapper mapper;

    public UserDatabase(String fileName) {
        this.fileName = fileName;
        mapper = new ObjectMapper();

        File file = new File(fileName);
        try {
            if(file.createNewFile()){
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, List.of());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        get();
    }


    public List<User> get() {
        List<User> users = new ArrayList<>();

        try (FileReader reader = new FileReader(fileName))
        {
            users = mapper.readValue(reader, new TypeReference<>() {
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

        return users;
    }

    public void add(User user){
        List<User> users = get();

        /* If it is a duplicate then update */
        if(users.stream().anyMatch(usr -> usr.getName().equals(user.getName()))){
            users = users
                    .stream()
                    .map(usr -> {
                        if(usr.getName().equals(user.getName())) return user;
                        return usr;
                    })
                    .collect(Collectors.toList());
        } else {
            users.add(user);
        }

        add(users);
    }

    public void add(List<User> users) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), users);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
