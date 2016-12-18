package com.az.gitember;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.az.gitember.misc.Pair;
import com.az.gitember.misc.ScmBranch;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.service.SettingsServiceImpl;
import com.az.gitember.ui.ScmItemCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */

public class FXMLController implements Initializable {


    @FXML
    public Button cloneBtn;

    @FXML
    public Button fetchBtn;

    @FXML
    public Button pullBtn;

    @FXML
    public Button pushBtn;

    @FXML
    public Button openBtn;

    @FXML
    public Menu openRecentMenuItem;

    @FXML
    private ListView localBranchesList;

    @FXML
    private ListView remoteBranchesList;

    @FXML
    private ListView tagList;

    @FXML
    private AnchorPane hostPanel;

    private GitRepositoryService repositoryService;

    private SettingsServiceImpl settingsService = new SettingsServiceImpl();

    @FXML
    public void openHandler(ActionEvent actionEvent) throws Exception {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (settingsService.getLastProject() != null) {
            directoryChooser.setInitialDirectory(new File(settingsService.getLastProject()));
        }
        File selectedDirectory =
                directoryChooser.showDialog(MainApp.getMainStage());
        if(selectedDirectory != null){
            String absPath = selectedDirectory.getAbsolutePath();
            if (!absPath.endsWith(".git")) {
                absPath += File.separator + ".git";
            }
            openRepository(absPath);
        }

    }

    private void openRepository(String absPath) throws Exception {
        repositoryService = new GitRepositoryService(absPath);
        localBranchesList.setItems(FXCollections.observableList(repositoryService.getLocalBranches()));
        remoteBranchesList.setItems(FXCollections.observableList(repositoryService.getRemoteBranches()));
        tagList.setItems(FXCollections.observableList(repositoryService.getTags()));
        settingsService.saveRepository(absPath);
    }

    @FXML
    public void exitActionHandler(ActionEvent actionEvent) {
        Platform.exit();
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final ScmItemCellFactory scmItemCellFactory = new ScmItemCellFactory();
        remoteBranchesList.setCellFactory(scmItemCellFactory);
        localBranchesList.setCellFactory(scmItemCellFactory);
        tagList.setCellFactory(scmItemCellFactory);

        try {

            openRecentMenuItem.getItems().removeAll(openRecentMenuItem.getItems());
            settingsService.getRecentProjects().stream().forEach(
                    o -> {
                        MenuItem mi = new MenuItem(o.getFirst());
                        mi.setUserData(o.getSecond());
                        mi.setOnAction(
                                event -> {
                                    try {
                                        openRepository(o.getSecond());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                        openRecentMenuItem.getItems().add(mi);
                    }

            );
            openRecentMenuItem.setDisable(
                    openRecentMenuItem.getItems().size() < 1
            );

        } catch (Exception e) {

        }


    }

    @FXML
    public void branchesListMouseClicked(Event event) {

        ScmBranch scmBranch = ((ListView<ScmBranch>) event.getSource()).getSelectionModel().getSelectedItem();

        try {
            final FXMLLoader fxmlLoader = new FXMLLoader();
            try (InputStream is = getClass().getResource("/fxml/BranchViewPane.fxml").openStream()) {
                final Parent branchView = fxmlLoader.load(is);
                final BranchViewController branchViewController = fxmlLoader.getController();

                branchViewController.setTreeName(scmBranch.getFullName());
                branchViewController.setRepositoryService(repositoryService);
                branchViewController.open();



                hostPanel.getChildren().removeAll(hostPanel.getChildren());
                hostPanel.getChildren().add(branchView);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void cloneHandler(ActionEvent actionEvent) {
    }

    public void fetchHandler(ActionEvent actionEvent) {
    }

    public void pullHandler(ActionEvent actionEvent) {
    }

    public void pushHandler(ActionEvent actionEvent) {
    }

    public void exitHandler(ActionEvent actionEvent) {
        Platform.exit();
    }
}
