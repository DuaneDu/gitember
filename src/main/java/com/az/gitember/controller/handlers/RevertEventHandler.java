package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.controller.common.DefaultProgressMonitor;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.Stage;
import com.az.gitember.service.Context;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.api.CheckoutCommand;

import java.util.logging.Logger;

public class RevertEventHandler extends AbstractLongTaskEventHandler implements EventHandler<ActionEvent> {

    private final static Logger log = Logger.getLogger(StageEventHandler.class.getName());

    private final ScmItem item;
    private final Stage stage;

    public RevertEventHandler(ScmItem item) {
        this.item = item;
        this.stage = null;
    }



    @Override
    public void handle(ActionEvent event) {
        if (item != null) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setWidth(LookAndFeelSet.DIALOG_DEFAULT_WIDTH);
            alert.setTitle("Question");
            alert.setContentText("Would you like to revert " + item.getShortName() + " changes ?");
            alert.initOwner(App.getScene().getWindow());
            alert.showAndWait().ifPresent( r -> {
                if (r == ButtonType.OK) {

                    Task<Void> longTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            Context.getGitRepoService().checkoutFile(item.getShortName(), CheckoutCommand.Stage.BASE );
                            Context.updateStatus(new DefaultProgressMonitor((t, d) -> {
                                updateTitle(t);
                                updateProgress(d, 1.0);
                            }, DefaultProgressMonitor.Strategy.Step));
                            return null;
                        }
                    };

                    launchLongTask(
                            longTask,o -> {},
                            o -> {
                                Context.getMain().showResult("Revert", "Failed to revert " + item.getShortName() + "\n" +
                                        ExceptionUtils.getStackTrace(longTask.getException()), Alert.AlertType.ERROR);
                            }
                    );
                }
            });

        }
    }

    /*private CheckoutCommand.Stage adaptStage(Stage stage) {
        if (stage == Stage.OURS) {
            return CheckoutCommand.Stage.OURS;
        } else if (stage == Stage.THEIRS) {
            return CheckoutCommand.Stage.THEIRS;
        }
        return CheckoutCommand.Stage.BASE;
    }*/

}
