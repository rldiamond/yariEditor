/*
 * This file is part of Yari Editor.
 *
 *  Yari Editor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Yari Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with Yari Editor. If not, see <http://www.gnu.org/licenses/>.
 */

package components;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import settings.Settings;

import java.io.File;

import static org.testfx.assertions.api.Assertions.assertThat;

public class RecommendedFileListViewTest extends ApplicationTest {
    RecommendedFileListView listView;
    Settings settings;

    @Override
    public void start(Stage stage) throws Exception {
        settings = new Settings();
        settings.addRecentFile(new File("/test"));
        listView = new RecommendedFileListView(null, new SimpleBooleanProperty(false), settings);
        Scene scene = new Scene(listView);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testFxProperties() {
        assertThat(listView).hasExactlyNumItems(1);
    }
}