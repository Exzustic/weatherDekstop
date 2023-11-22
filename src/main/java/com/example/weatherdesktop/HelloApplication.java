package com.example.weatherdesktop;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Weather");

        TextField searchInput = new TextField();
        Label searchResult = new Label();
        searchResult.setPadding(new Insets(0, 0, 0, 20));

        Button searchButton = new Button("Search"); // Создание кнопки Search
        searchButton.setOnAction(e -> {
            String[] query = searchInput.getText().split(",");
            searchInput.setText("");
            String apiUrlButton = "http://localhost:8080/weather?countryCode=" + query[0].trim() + "&cityName=" + query[1].trim();
            try {
                URL url = new URL(apiUrlButton);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder responseButton = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        responseButton.append(inputLine);
                    }
                    in.close();
                    Gson gson = new Gson();
                    JsonObject jsonResponse = gson.fromJson(String.valueOf(responseButton), JsonObject.class);

                    JsonArray weatherArray = jsonResponse.getAsJsonArray("weather");
                    JsonObject main = jsonResponse.getAsJsonObject("main");
                    JsonObject wind = jsonResponse.getAsJsonObject("wind");

                    String weatherMain = weatherArray.get(0).getAsJsonObject().get("main").getAsString();
                    String weatherDescription = weatherArray.get(0).getAsJsonObject().get("description").getAsString();
                    double temp = Math.round((main.get("temp").getAsDouble() - 273.15) * 10.0) / 10.0;
                    double feelsLike = Math.round((main.get("feels_like").getAsDouble() - 273.15) * 10.0) / 10.0;
                    double tempMin = Math.round((main.get("temp_min").getAsDouble() - 273.15) * 10.0) / 10.0;
                    double tempMax = Math.round((main.get("temp_max").getAsDouble() - 273.15) * 10.0) / 10.0;
                    double windSpeed = wind.get("speed").getAsDouble();
                    int windDeg = wind.get("deg").getAsInt();

                    String formattedWeather = "The weather in " + query[1].trim().substring(0, 1).toUpperCase() + query[1].trim().substring(1) + ":\n" +
                            "Weather: " + weatherMain + " (" + weatherDescription + ")\n" +
                            "Temp: " + temp + "°C\n" +
                            "Feels like: " + feelsLike + "°C\n" +
                            "Temp min: " + tempMin + "°C\n" +
                            "Temp max: " + tempMax + "°C\n" +
                            "Wind speed: " + windSpeed + " m/s\n" +
                            "Wind from: " + convertWindDirection(windDeg);

                    searchResult.setText(formattedWeather);
                } else {
                    searchResult.setText(query[1].trim() + " wasn't found in " + query[0].trim());
                }
            } catch (Exception ex) {
                searchResult.setText("Error: " + ex.getMessage());
                System.out.println(ex.getMessage());
            }
        });

        searchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                String apiUrl;
                if (newValue.contains(",") && newValue.split(",").length > 1) {
                    String[] values = newValue.split(",");
                    apiUrl = "http://localhost:8080/searches?country=" + values[0].trim() + "&city=" + values[1].trim();
                } else {
                    apiUrl = "http://localhost:8080/search?query=" + newValue.trim();
                }

                Task<String> task = new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        URL url = new URL(apiUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String inputLine;
                            StringBuilder response = new StringBuilder();
                            int count = 0;

                            while ((inputLine = in.readLine()) != null && count < 5) {
                                response.append(inputLine);
                                count++;
                            }
                            in.close();
                            return response.toString();
                        } else {
                            return "Error: " + responseCode;
                        }
                    }
                };

                task.setOnSucceeded(event -> {
                    String response = task.getValue();
                    List<String> result = Arrays.asList(response.replaceAll("(\\[\")|(\"\\])", "").split("\",\""));

                    // Создание контейнера для кнопок
                    VBox buttonBox = new VBox(5);
                    buttonBox.setPadding(new Insets(-20, 0, 0, 20));
                    // Добавление кнопок в контейнер
                    for (String item : result) {
                        Button itemButton = new Button(item);
                        itemButton.setOnAction(event1 -> searchInput.setText(item));
                        buttonBox.getChildren().add(itemButton);
                    }
                    VBox root = new VBox(10);

                    GridPane grid = new GridPane();
                    grid.setPadding(new Insets(20));
                    grid.setHgap(10);

                    grid.add(searchInput, 0, 0);
                    HBox hbox = new HBox();
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);



                    hbox.getChildren().add(searchButton); // Добавляем кнопку в контейнер для панели
                    hbox.setHgrow(searchButton, Priority.ALWAYS);
                    grid.add(hbox, 1, 0);


                    root.getChildren().addAll(grid, buttonBox, searchResult); // Добавляем контейнер с кнопками после поля ввода

                    /*if (!searchInput.getText().isEmpty()) {
                        // Добавляем контейнер с кнопками после поля ввода
                        root.getChildren().remove(buttonBox);
                        root.getChildren().add(buttonBox);
                    } else {
                        // Если поле ввода пустое, убираем кнопки
                        root.getChildren().remove(buttonBox);
                    }*/

                    primaryStage.setScene(new Scene(root, 300, 360));
                    primaryStage.show();

                    searchInput.requestFocus(); // Убираем выделение текста
                    searchInput.selectEnd(); // Перемещаем курсор в конец текста

                });

                task.setOnFailed(event -> {
                    String errorMessage = "Error: " + task.getException().getMessage();
                    searchResult.setText(errorMessage);
                });

                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            } else {
                // Очистка результата, если поле ввода пустое
                searchResult.setText("");
            }
        });

        VBox root = new VBox(10);
        root.getChildren().addAll(searchInput, searchResult);

        primaryStage.setScene(new Scene(root, 300, 360));
        primaryStage.show();
    }

    private String convertWindDirection(int degrees) {
        String[] directions = {"north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest"};
        int index = (int) ((degrees / 45.0) + 0.5) % 8;
        return directions[index];
    }

    public static void main(String[] args) {
        launch(args);
    }
}
