package utilities;

import com.jfoenix.controls.JFXSnackbar;
import com.thoughtworks.xstream.XStream;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.yari.core.BasicRule;
import org.yari.core.Context;
import org.yari.core.YariException;
import org.yari.core.table.Action;
import org.yari.core.table.Condition;
import org.yari.core.table.DecisionTable;
import org.yari.core.table.Row;
import view.RootLayoutFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

//TODO: Save. Print. Shortcuts to open, save, print. On save show a toast notification.
public class FileUtil {

    private static File currentFile;

    public static boolean openFile(Window window) throws FileNotFoundException, YariException {
        FileChooser fileChooser = new FileChooser();

        // Set the extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            return importFromFile(file);
        }
        return false;
    }

    public static void saveToFile(File file) {

        DecisionTableValidator.updateTable();
        DecisionTableValidator.runValidation();

        if (!DecisionTableValidator.validProperty().get()) {
            return;
        }

        XStream xstream = new XStream();
        xstream.processAnnotations(DecisionTable.class);

        try (FileOutputStream out = new FileOutputStream(file)) {
            xstream.toXML(RootLayoutFactory.getInstance().getDecisionTable(), out);
            currentFile = file;
            ToastUtil.sendToast("File saved.");
        } catch (Exception ex) {
            ToastUtil.sendPersistantToast("Failed to save file! " + ex.getMessage());
        }
    }

    private static boolean importFromFile(File file) throws FileNotFoundException, YariException {
        clearData();

        if (!DecisionTableValidator.isValidXML(file.getPath())) {
            return false;
        }

        DecisionTable decisionTable;

        //get around some weird yari stuff to import the table
        BasicRule basicRule = new BasicRule() {
            @Override
            public void lookupGlobals(Context globalContext) {
            }
        };

        currentFile = file;

        decisionTable = basicRule.createDecisionTable(file.getPath());
        for (Condition condition : decisionTable.getConditions()) {
            RootLayoutFactory.getInstance().getConditionsList().add(condition);
        }
        for (Action action : decisionTable.getActions()) {
            RootLayoutFactory.getInstance().getActionsList().add(action);
        }
        var rowNumber = 0;
        for (Row row : decisionTable.getRawRowData()) {
            row.setRowNumber(rowNumber++);
            RootLayoutFactory.getInstance().getRowsList().add(row);
        }


        RootLayoutFactory.getInstance().setDecisionTable(decisionTable);
        return true;
    }

    public static void clearData() {
        RootLayoutFactory.getInstance().getRowsList().clear();
        RootLayoutFactory.getInstance().getActionsList().clear();
        RootLayoutFactory.getInstance().getConditionsList().clear();
        RootLayoutFactory.getInstance().setDecisionTable(null);
    }

    public static File getCurrentFile() {
        return currentFile;
    }
}
