# A Java Web Scraping App using Jsoup and JavaFX

## Introduction

The web has been an entropic explosion of data onto the world and, in recent years it has been shown that enormous value can be gleaned from even seaminly innocuous text data such as the blog post you're reading now. In this article I demonstrate how to use the Jsoup Java library to fetch and parse text data from web pages paired with simple query and display functionality wrapped in a JavaFX desktop app, named ScraperFX, running on OpenJDK 12.

#### Contents

## Introducing the Jsoup Library

In order to manipulate and extract content from HTML using Jsoup you first must instruct Jsoup to parse it into its internal data structure. Given simple HTML strings this can be accomplished with the org.jsoup.Jsoup#parse static method which returns an instance of org.jsoup.nodes.Document. 

I can easily demonstrate this using JShell after downloading the Jsoup core library jar file and adding it to my classpath as shown below.

```
$ ls
jsoup-1.12.1.jar
$ export CLASSPATH=jsoup-1.12.1.jar
$ jshell
|  Welcome to JShell -- Version 12
|  For an introduction type: /help intro

jshell> import org.jsoup.Jsoup;

jshell> import org.jsoup.nodes.Document;

jshell> String html = "<html>\n<head><title>Howdy Java Lovers</title></head>\n";
html ==> "<html>\n<head><title>Howdy Java Lovers</title></head>\n"

jshell> html += "<body><h1 id='header'>Welcome to <span class='brand'>The Coding Interface</span>'s Article on Java Web Scraping.</h1></body>\n</html>";
$4 ==> "<html>\n<head><title>Howdy Java Lovers</title></head>\n<body><h1 id='header'>Welcome to <span class='brand'>The Coding Interface</span>'s Article on Java Web Scraping.</h1></body>\n</html>"

jshell> Document doc = Jsoup.parse(html);
doc ==> <html>
 <head>
  <title>Howdy Java Lovers</title> ... ng.</h1> 
 </body>
</html>
```

Jsoup provides convient methods to grab common HTML elements like Document#title as well as ways to query HTML data parsed into a Document object through simple CSS query selector-like syntax using the Document#select method. Continuing from my last example and still in JShell I demonstrate both methods.

```
jshell> doc.title();
$6 ==> "Howdy Java Lovers"

jshell> doc.select("h1");
$7 ==> <h1 id="header">Welcome to <span class="brand">The Coding Interface</span>'s Article on Java Web Scraping.</h1>

jshell> doc.select("#header");
$8 ==> <h1 id="header">Welcome to <span class="brand">The Coding Interface</span>'s Article on Java Web Scraping.</h1>

jshell> doc.select(".brand");
$9 ==> <span class="brand">The Coding Interface</span>
```

The returned type of the various calls to Document#select is org.jsoup.nodes.Elements which represent HTML tags (aka elements). The Elements class extends ArrayList from the collections framework making it an iterable container of org.jsoup.nodes.Element objects.

```
jshell> import org.jsoup.select.Elements;

jshell> import org.jsoup.nodes.Element;

jshell> Elements elements = doc.select("h1");
elements ==> <h1 id="header">Welcome to <span class="brand">Th ... on Java Web Scraping.</h1>

jshell> for (Element el : elements) {
   ...>   System.out.println("Tag: " + el.tagName() + " contains " + el.text());
   ...> }
Tag: h1 contains Welcome to The Coding Interface's Article on Java Web Scraping.
```

As you can see from the above JShell snippet the Element class has methods for accessing meaningful data for each HTML element such as Element#tagName and Element#text along with many others so be sure to further investigate the official Jsoup API docs.

## Project Setup

For the ScraperFX demo application I will utilize the Gradle build system with the JavaFX plugin, see my earlier blog post __[JavaFX with Gradle, Eclipse and, Scene Builder on OpenJDK11: Project Setup](https://thecodinginterface.com/blog/javafx-dev-setup-gradle-and-eclipse/)__ if you are unsure of how to set things up. The things that will differ in this project are that I specify the Jsoup library as an implementation dependency of the project and the base package of the project is com.thecodinginterface.scarperfx.

Below is my build.gradle file.

```

plugins {
    // Apply the java plugin to add support for Java
    id 'java'

    // Apply the application plugin to add support for building an application
    id 'application'

    id 'org.openjfx.javafxplugin' version '0.0.7'
}

repositories {
    mavenCentral()
}

javafx {
    version = "12"
    modules = [ 'javafx.controls', 'javafx.fxml' ]
}

dependencies {
    // This dependency is found on compile classpath of this component and consumers.
    implementation 'com.google.guava:guava:27.0.1-jre'

    implementation 'org.jsoup:jsoup:1.12.1'

    // Use JUnit test framework
    testImplementation 'junit:junit:4.12'
}

// Define the main class for the application
mainClassName = 'com.thecodinginterface.scraperfx.App'
```

## Designing the FXML UI with Scene Builder

The ScraperFX app consists of just one FXML view file, found at src/main/resources/com/thecodinginterface/scraperfx/ScraperFX.fxml, which utilizes a BorderPane as the root scene Node. The top section holds a HBox to serve as a header component complete, left to right, with a Label wrapping a ImageView displaying The Coding Interface's logo (located at src/main/resources/com/thecodinginterface/scraperfx/tci-nav-logo.png) followed by a TextField for entering a URL of a web page to fetch and parse, and finally a Button for submitting the URL.

*** BorderPane-RawHeader.png ***

Obviously this UI needs a little sprucing up so, I add a JavaFX CSS stylesheet to the Scene Builder design tool and give it the following style rules. The styles.css style sheet shown below is at src/main/resources/com/thecodinginterface/scraperfx/styles.css

```
.header-hbox {
    -fx-background-color: #03a9f4;
    -fx-padding: 8 20 8 20;
    -fx-spacing: 10;
    -fx-alignment: CENTER;
}

.logo-label {
    -fx-background-color: #edf3f5;
    -fx-background-insets: 4;
    -fx-background-radius: 12;
    -fx-padding: 3 2 3 2;
}

.text-field {
    -fx-padding: 10 8 10 8;
}

.button {
    -fx-padding: 10 10 10 10;
    -fx-background-color: #0871a5;
    -fx-text-fill: white;
}

.button:hover {
  -fx-background-color: #0a3f5a;
}

.button:pressed, .button:disabled {
  -fx-background-color: #0d76aa;
}
```

After adding the .header-hbox style class to the HBox and .logo-label style class to the Label nodes and expanding the width of the TextField things look quite a bit more appealing.

*** BorderPane-StyledHeader.png ***

If you are unsure of how to add the stylesheet or the CSS style classes to the Nodes in Scene Builder I have detailed how this is done in my previous article __[JavaFX with Gradle, Eclipse, Scene Builder and OpenJDK 11: Refactor with FXML and Scene Builder](https://thecodinginterface.com/blog/javafx-with-gradle-eclipse-fxml-scenebuilder/)__

In addition to styling the HBox and its contents I add fx:id values for TextField of urlTextField as well as fetchBtn for the Button.

To the center section of the root BorderPane I add an AnchorPane followed by a horizontal SplitPane on top of it and anchor the SplitPane to the edge of all four sides of the AnchorPane.

*** BorderPane-AnchoredSplitPane.png ***

The left side of the SplitPane will contain the raw HTMl markup fetched using Jsoup which may be quite long so, I add a ScrollPane to the AnchorPane that makes up the left side of the SplitPane and anchor the ScrollPane to all four edges again.

*** BorderPane-LeftScrollPane-Anchored.png ***

Then on the AnchorPane that gets added as part of the ScrollPane I add a TextArea control and again anchor it to the four edges of it's parent AnchorPane. In addition to anchoring the TextArea I also make it uneditable and tell it to wrap text. To access the TextArea control from with my Java code I add a fx:id value of docTextArea.

*** BorderPane-LeftScrollPaneWithTextArea.png ***

Switching focus over to the right side of the SplitPane where the interactively queried elements appear I add a BorderPane to that side's AnchorPane and anchor it to all four sides. Then in the bottom section of the BorderPane I add a HBox and to the HBox I add a TextField. Within the Layout section of the Inspector menu on the right of Scene Builder I set all the size specifications to USE_COMPUTED_SIZE and for the TextField I set the Hgrow field to ALWAYS. The TextField will also need to be interacted with from Java code so it gets a fx:id value of queryTextField.

*** BorderPane-RightSplitPane-QueryField.png ***

Then to the center section of the newly added BorderPane on the right I add a AnchorPane followed by a ScrollPane which of course I anchor to four sides of the parent AnchorPane. To the ScrollPane I add an Accordion which will hold entries for the queried Elements selected from the Jsoup Document object. Since the Accordion control needs to be populated with TitlePanes within the Java code it will get an fx:id of elementsAccordion.

*** BorderPane-RightSplitPane-Accordion.png ***

I also add a On Action event handler (ie, click handler) for the Fetch Page button within the Code submenu on the right side of Scene Builder.

*** BorderPane-HandleFetchPage.png ***

Lastly I configure the ScraperFX.fxml file within Scene Builder to use com.thecodinginterface.scraperfx.Controller as the controller class which I code up in the next section. 

*** BorderPane-AddController.png ***

## Fetching and Parsing Web Pages by URL using Jsoup

To get the JavaFX project interacting with the ScraperFX.fxml view I designed in the previous section I add the Controller.java file to the src/main/java/com/thecodinginterface/scraperfx directory (aka package). I can get a bit of a head start on building the Controller.java file by opening the View menu of Scene Builder and clicking the Show Sample Controller Skeleton menu item then copying the starter code and pasting it into the Controller.java source file.

*** ControllerSkeleton.png ***

Inside the Controller class I implement the Initializable interface from the javafx.fxml package and add the Initializable#initialize method stub as shown below.

```
// Controller.java

package com.thecodinginterface.scraperfx;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

    @FXML
    void handleFetchPane(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
      
    }
}

```

With the basic Controller class stubbed out I can now put it to use in the App.java main class created by the Gradle's init command I used to scaffold out the project structure. To start I clear out the contents App.java and replace it with the following.

```
// App.java

package com.thecodinginterface.scraperfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // instantiate FXMLoader and load the ScraperFX.fxml's BorderPane
        var fxmlLoader = new FXMLLoader(getClass().getResource("ScraperFX.fxml"));
        var borderPane = (BorderPane) fxmlLoader.load();

        // create a Scene object and source the styles.css stylesheet
        var scene = new Scene(borderPane);
        scene.getStylesheets().add(
            getClass().getResource("styles.css").toExternalForm()
        );

        // set main window title, associate the scene and show the window
        primaryStage.setTitle("ScraperFX");
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
```

As you can see I extend the javafx.application.Application class, load the ScraperFX.fxml view file built earlier along with the styles.css stylesheet then wrap the root BorderPane node loaded with the FXMLLoader class in a Scene object and show it in the Stage window.

At this point I can issue the Gradle run task to make sure everything loads properly.

```
$ ./gradlew run
```

*** ScraperFX-No-Content.png ***

Now back over in the Controller#initialize method I add a constraint to the fetchBtn Button class member field to make it disabled if the urlTextField control is empty.

```
@Override
public void initialize(URL location, ResourceBundle resources) {
    fetchBtn.disableProperty().bind(
        urlTextField.textProperty().isEmpty()
    );
}
```

In the Controller I add a new member field of type Document from the Jsoup library and name it doc then in the Controller#handleFetchPage method I call the Jsoup#connect static method passing it the URL entered into the urlTextField by the user before finally calling the get() method the resulting Connection object. The result is assigned  to the new doc member variable. 

Using the doc variable I call body() which returns an Element object then I chain another call to html() to get a string of HTML which I set to the docTextArea TextArea control. Note that the connection object may throw an IOException so I wrap things in a try / catch block.

```
private Document doc;

@FXML
void handleFetchPage(ActionEvent event) {
    try {
        doc = Jsoup.connect(urlTextField.getText()).get();
        docTextArea.setText(doc.body().html());
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

Now if I reload the application and give it a valid URL such as https://thecodinginterface.com/blog/java-collections-stream-cloning/ and click the Fetch Page button this is what I see.

*** ScraperFX-FetchPage.png ***


## Interactively Querying the Jsoup Document

ScraperFX is now capable of fetching a web page and displaying its HTML markup so, the remaining functionality is the ability to query the Jsoup Document object and display the selected elements in the right SplitPane based off the selector typed into the query selector text field. To react to the user typing in their query selector I attach an event listener to the queryTextField TextField#textProperty. However, to give a better user experience I utilizing a debounce like feature that limits the frequency at which text input events are responded to and applied to query the Document variable.

For implementing the debounce functionality I use the javafx.animation.PauseTransition class, adding it as a new class member variable named debouncer. Then back in the initialize method I instantiate PauseTransition instructing it to run for a duration (aka pause) of 1 second. Next I add the text field event listener which initiates the debouncer after the second has elapsed and check to see if some text has been entered. If there is input then its used to query the doc variable for elements which I use to build and populate the Accordion control with TitledPane panes. The TitledPane represents the expandable node within an Accordion which in the case of ScraperFX is just another TextArea containing the html for each element all wrapped in a StackPane.

Below is the complete Controller.java source file.

```
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
```


## Learn More About JavaFX and Web Scraping with JSoup in Java



## Conclusion