// Controller.java

package com.thecodinginterface.scraperfx;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Controller implements Initializable {

    @FXML
    private TextField urlTextField;

    @FXML
    private Button fetchBtn;

    @FXML
    private TextArea docTextArea;

    @FXML
    private TextField queryTextField;

    @FXML
    private Accordion elementsAccordion;

    private Document doc;
    private PauseTransition debouncer;

    @FXML
    void handleFetchPage(ActionEvent event) {
        try {
            // create a Connection object then call get() on it
            // and assign the resulting Document to the doc variable
            doc = Jsoup.connect(urlTextField.getText()).get();

            // populate the HTML TextArea with the Document's body html
            docTextArea.setText(doc.body().html());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // only allow clicking the Fetch Page Button if there is 
        // a url entered into the URL Text Field
        fetchBtn.disableProperty().bind(
            urlTextField.textProperty().isEmpty()
        );

        // create and configure the PauseTransition to pause for a
        // second to simulate a debouncer
        debouncer = new PauseTransition(Duration.seconds(1));

        // listen to text input events being added (or removed) to the query
        // selector input TextField
        queryTextField.textProperty().addListener((obs, ov, nv) -> {
            // add and event handler to run when the debouncer is 
            // finished it's one second pause
            debouncer.setOnFinished(evt -> {
                // start with a empty Accordion each time
                elementsAccordion.getPanes().clear();

                var querySelector = queryTextField.getText();
                if (querySelector != null && !querySelector.isBlank()) {
                    
                    // query the Document object based off input from the user
                    Elements elements = doc.select(querySelector);
                    elements.forEach(el -> {
                        // add the HTML contents of each Element object 
                        // to the accordion in the form of a TextArea wrapped in
                        // a TitledPane
                        var textArea = new TextArea(el.html());
                        textArea.setWrapText(true);
                        textArea.setEditable(false);
                        textArea.setPrefSize(600, 200);
                        elementsAccordion.getPanes().add(
                            new TitledPane(el.tagName(), new StackPane(textArea))
                        );
                    });
                }
            });
            debouncer.playFromStart();
        });
    }
}
