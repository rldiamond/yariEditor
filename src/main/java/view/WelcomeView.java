/*
 * This file is part of Yari Editor.
 *
 *  Yari Editor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Yari Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with Yari Editor. If not, see <http://www.gnu.org/licenses/>.
 */
package view;

import com.jfoenix.controls.JFXSpinner;
import components.RecommendedFileListView;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import settings.SettingsDialog;
import utilities.FXUtil;
import utilities.FileUtil;
import utilities.SettingsUtil;
import utilities.ThemeUtil;
import utilities.resizing.ResizeHelper;

import java.io.File;

/**
 * View displayed when the application is first launched. Provides extra
 * direction for users such as create a new table, open a saved table from a
 * file, change settings as well as listing recommended files.
 */
public class WelcomeView extends VBox {

    private BooleanProperty busy = new SimpleBooleanProperty(false);

    /**
     * Construct the WelcomeView with the supplied stage.
     *
     * @param stage the stage to utilize in various tasks.
     */
    public WelcomeView(Stage stage) {
        setId("welcomeSplash");
        super.setPrefSize(575, 350);
        super.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        super.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

        //header area
        //dragbar
        StackPane dragBar = new StackPane();
        dragBar.setPrefSize(USE_COMPUTED_SIZE, 25);
        dragBar.setMinSize(USE_COMPUTED_SIZE, USE_PREF_SIZE);
        dragBar.setMaxSize(USE_COMPUTED_SIZE, USE_PREF_SIZE);
        dragBar.setAlignment(Pos.CENTER_RIGHT);
        dragBar.setPadding(new Insets(5, 5, 5, 5));

        //allow dragging
        ResizeHelper.addUndecoratedStageDragListener(stage, dragBar);

        //close button
        Pane closeButton = new Pane();
        closeButton.setPrefSize(16, 16);
        closeButton.setMinSize(16, 16);
        closeButton.setMaxSize(16, 16);
        closeButton.setId("closeButton");
        closeButton.setOnMouseClicked(me -> Platform.exit());
        Tooltip.install(closeButton, new Tooltip("Exit"));
        dragBar.getChildren().add(closeButton);

        //Content
        HBox contentContainer = new HBox();
        VBox.setVgrow(contentContainer, Priority.ALWAYS);

        //Left Content
        StackPane leftContainerPane = new StackPane();
        leftContainerPane.setPrefSize(260, USE_COMPUTED_SIZE);
        leftContainerPane.setMinSize(USE_PREF_SIZE, USE_COMPUTED_SIZE);
        leftContainerPane.setMaxSize(USE_PREF_SIZE, USE_COMPUTED_SIZE);
        contentContainer.getChildren().add(leftContainerPane);
        RecommendedFileListView recommendedFileListView = new RecommendedFileListView(stage, busy, SettingsUtil.getSettings());
        recommendedFileListView.disableProperty().bind(busy);
        HBox.setHgrow(recommendedFileListView, Priority.ALWAYS);
        leftContainerPane.getChildren().setAll(recommendedFileListView);

        //Right Content
        VBox rightContent = new VBox(10);
        rightContent.setPadding(new Insets(5, 5, 5, 5));
        rightContent.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(rightContent, Priority.ALWAYS);
        contentContainer.getChildren().add(rightContent);
        rightContent.getChildren().add(dragBar);

        //Logo
        ImageView logoView = new ImageView();
        logoView.setImage(ThemeUtil.getLogo());
        logoView.setFitHeight(80);
        logoView.setFitWidth(80);
        HBox logoWrapper = new HBox(logoView);
        VBox.setMargin(logoWrapper, new Insets(25, 0, 0, 0));
        logoWrapper.setAlignment(Pos.CENTER);
        rightContent.getChildren().add(logoWrapper);

        //title
        Label title = new Label("Yari Editor");
        title.setId("titleLabel");
        rightContent.getChildren().add(title);

        //create new
        HBox createNewContainer = new HBox(8);
        createNewContainer.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(createNewContainer, new Insets(30, 0, 0, 110));
        Pane createNewIcon = new Pane();
        createNewIcon.setPrefSize(10, 13);
        createNewIcon.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        createNewIcon.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        createNewIcon.setId("fileIcon");
        Label createNewLabel = new Label("Create New");
        Tooltip.install(createNewContainer, new Tooltip("Create a new document"));
        createNewLabel.setId("splashLink");
        createNewLabel.setOnMouseClicked(me -> FXUtil.runAsync(() -> {
            FileUtil.newFile();
            FXUtil.runOnFXThread(() -> RootLayoutFactory.show(stage));
        }));
        createNewContainer.getChildren().addAll(createNewIcon, createNewLabel);
        createNewContainer.disableProperty().bind(busy);
        rightContent.getChildren().add(createNewContainer);

        //open
        HBox openContainer = new HBox(5);
        openContainer.setAlignment(Pos.CENTER_LEFT);
        Tooltip.install(openContainer, new Tooltip("Open a document from a file"));
        VBox.setMargin(openContainer, new Insets(0, 0, 0, 108));
        Pane openIcon = new Pane();
        openIcon.setPrefSize(16, 12);
        openIcon.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        openIcon.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        openIcon.setId("openIcon");
        Label openLabel = new Label("Open");
        openLabel.setId("splashLink");
        openLabel.setOnMouseClicked(me -> {
            busy.set(true);
            boolean success = FileUtil.openDecisionTableFile(stage, busy);
            if (!success) {
                busy.set(false);
            }
        });
        openContainer.getChildren().addAll(openIcon, openLabel);
        openContainer.disableProperty().bind(busy);
        rightContent.getChildren().add(openContainer);


        //import
        HBox importContainer = new HBox(10);
        importContainer.setAlignment(Pos.CENTER_LEFT);
        Tooltip.install(importContainer, new Tooltip("Import a table from Excel"));
        VBox.setMargin(importContainer, new Insets(0, 0, 0, 108));
        Pane importIcon = new Pane();
        importIcon.setPrefSize(12, 13);
        importIcon.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        importIcon.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        importIcon.setId("importIcon");
        Label importLabel = new Label("Import");
        importLabel.setId("splashLink");
        importContainer.disableProperty().bind(busy);
        importContainer.getChildren().addAll(importIcon, importLabel);
        importLabel.setOnMouseClicked(me -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xls)", "*.xlsx");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(getScene().getWindow());
            FXUtil.runAsync(() -> {
                busy.set(true);
                if (RootLayoutFactory.getInstance().importExcel(file)) {
                    FXUtil.runOnFXThread(() -> RootLayoutFactory.show(stage));
                }
                busy.set(false);
            });

        });
        rightContent.getChildren().add(importContainer);


        //busy indicator
        JFXSpinner busySpinner = new JFXSpinner();
        busySpinner.setRadius(25);
        busySpinner.visibleProperty().bind(busy);
        busySpinner.managedProperty().bind(busy);
        rightContent.getChildren().add(busySpinner);

        //settings icon
        StackPane settingsContainer = new StackPane();
        VBox.setVgrow(settingsContainer, Priority.ALWAYS);
        settingsContainer.setAlignment(Pos.BOTTOM_RIGHT);
        Pane settingsButton = new Pane();
        Tooltip.install(settingsButton, new Tooltip("Settings"));
        settingsButton.setOnMouseClicked(e -> {
            SettingsDialog settingsDialog = new SettingsDialog(SettingsUtil.getSettings(), stage.getOwner());
            settingsDialog.show();
        });
        settingsButton.setId("settingsButton");
        settingsButton.setPrefSize(14, 14);
        settingsButton.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        settingsButton.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        settingsContainer.getChildren().add(settingsButton);
        rightContent.getChildren().add(settingsContainer);

        super.getChildren().setAll(contentContainer);

    }

}
