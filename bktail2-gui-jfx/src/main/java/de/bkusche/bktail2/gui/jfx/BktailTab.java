/**
 * Copyright 2018 Björn Kusche
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bkusche.bktail2.gui.jfx;

import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author bkusche
 *
 * This class is based on code by Michael Berry (https://berry120.blogspot.de/2014/01/draggable-and-detachable-tabs-in-javafx.html)
 * and Jens Deters (http://www.jensd.de/wordpress/?p=1332)
 */
public class BktailTab extends Tab implements I_TailActionEventListener {

    private static final Set<TabPane> tabPanes;
    private static final Stage markerStage;
    private HBox control;
    private Label label;
    private CheckBox checkBox;
    private Text dragText;
    private Stage dragPreview;
    private boolean detachable;
    private I_TailActionEventListener tailActionEventListener;

    static {
        tabPanes = new HashSet<>();
        markerStage = new Stage();
        markerStage.initStyle(StageStyle.UNDECORATED);
        Rectangle dummy = new Rectangle(3, 20, Color.web("#ff00e4"));
        StackPane markerStack = new StackPane();
        markerStack.getChildren().add(dummy);
        markerStage.setScene(new Scene(markerStack));
    }


    public BktailTab(String text) {
        checkBox = new CheckBox();
        checkBox.setOnAction(event -> {
            if( tailActionEventListener != null)
                tailActionEventListener.onTailChangedActionEvent(checkBox.isSelected());
        });
        label = new Label(text);
        control = new HBox();
        control.getChildren().add(checkBox);
        control.getChildren().add(label);
        setGraphic(control);
        detachable = true;
        dragPreview = new Stage();
        dragPreview.initStyle(StageStyle.UNDECORATED);
        dragText = new Text(text);

        control.setOnMouseDragged(mouseEvent -> {
            try {
                displayDraggedTabPreview(mouseEvent);

                Point2D screenPoint = new Point2D(mouseEvent.getScreenX(), mouseEvent.getScreenY());
                tabPanes.add(getTabPane());
                InsertData data = getInsertData(screenPoint);
                if(data == null || data.getInsertPane().getTabs().isEmpty()) {
                    markerStage.hide();
                }
                else {
                    int index = data.getIndex();
                    boolean end = false;
                    if(index == data.getInsertPane().getTabs().size()) {
                        end = true;
                        index--;
                    }
                    Rectangle2D rect = getAbsoluteRect(data.getInsertPane().getTabs().get(index));
                    if(end) {
                        markerStage.setX(rect.getMaxX() + 5);
                    }
                    else {
                        markerStage.setX(rect.getMinX()-10);
                    }
                    markerStage.setY(rect.getMaxY() + 5);
                    markerStage.show();
                }
            } finally {
                mouseEvent.consume();
            }
        });

        control.setOnMouseReleased(mouseEvent-> {
            try {
                markerStage.hide();
                dragPreview.hide();

                if(mouseEvent.isStillSincePress()) {
                    return;
                }

                Point2D screenPoint = new Point2D(mouseEvent.getScreenX(), mouseEvent.getScreenY());
                TabPane oldTabPane = getTabPane();
                int oldIndex = oldTabPane.getTabs().indexOf(BktailTab.this);
                tabPanes.add(oldTabPane);
                InsertData insertData = getInsertData(screenPoint);

                if(insertData != null) {
                    int addIndex = insertData.getIndex();
                    if(oldTabPane == insertData.getInsertPane() && oldTabPane.getTabs().size() == 1) {
                        return;
                    }
                    oldTabPane.getTabs().remove(BktailTab.this);

                    if(oldIndex < addIndex && oldTabPane == insertData.getInsertPane()) {
                        addIndex--;
                    }

                    if(addIndex > insertData.getInsertPane().getTabs().size()) {
                        addIndex = insertData.getInsertPane().getTabs().size();
                    }
                    insertData.getInsertPane().getTabs().add(addIndex, BktailTab.this);
                    insertData.getInsertPane().selectionModelProperty().get().select(addIndex);
                    return;
                }

                if(detachable) {
                    detachNewStage(mouseEvent);
                }
            }
            finally{
                mouseEvent.consume();
            }
        });
    }

    public void setTailActionEventListener(I_TailActionEventListener tailActionEventListener) {
        this.tailActionEventListener = tailActionEventListener;
    }

    @Override
    public void onTailChangedActionEvent(boolean selected) {
        checkBox.setSelected(selected);
    }

    private void displayDraggedTabPreview(MouseEvent mouseEvent) {
        if( !dragPreview.isShowing() ) {
            dragPreview.setScene(null);
            SnapshotParameters snapshotParams = new SnapshotParameters();
            snapshotParams.setTransform(Transform.scale(0.4, 0.4));
            WritableImage snapshot = getContent().snapshot(snapshotParams, null);
            VBox dragStagePane = new VBox();
            dragStagePane.setStyle("-fx-background-color:#DDDDDD;");
            StackPane.setAlignment(dragText, Pos.CENTER);
            dragStagePane.getChildren().add(dragText);
            dragStagePane.getChildren().add(new ImageView(snapshot));
            dragPreview.setScene(new Scene(dragStagePane));
            dragPreview.setWidth(snapshot.getWidth() + 2);
            dragPreview.setHeight(label.getHeight() + snapshot.getHeight() + 2);
        }
        dragPreview.setX(mouseEvent.getScreenX());
        dragPreview.setY(mouseEvent.getScreenY());
        dragPreview.show();
    }


    private InsertData getInsertData(Point2D screenPoint) {
        for(TabPane tabPane : tabPanes) {
            Rectangle2D tabAbsolute = getAbsoluteRect(tabPane);
            if(!tabAbsolute.contains(screenPoint) || tabPane.getTabs().isEmpty()) {
                continue;
            }
            int tabInsertIndex = 0;

            Rectangle2D firstTabRect = getAbsoluteRect(tabPane.getTabs().get(0));
            if(firstTabRect.getMaxY()+60 < screenPoint.getY() || firstTabRect.getMinY() > screenPoint.getY()) {
                return null;
            }

            Rectangle2D lastTabRect = getAbsoluteRect(tabPane.getTabs().get(tabPane.getTabs().size() - 1));
            if(screenPoint.getX() < (firstTabRect.getMinX() + firstTabRect.getWidth() / 2)) {
                tabInsertIndex = 0;
            }
            else if(screenPoint.getX() > (lastTabRect.getMaxX() - lastTabRect.getWidth() / 2)) {
                tabInsertIndex = tabPane.getTabs().size();
            }
            else {
                for(int i = 0; i < tabPane.getTabs().size() - 1; i++) {
                    Tab leftTab = tabPane.getTabs().get(i);
                    Tab rightTab = tabPane.getTabs().get(i + 1);
                    if(leftTab instanceof BktailTab && rightTab instanceof BktailTab) {
                        Rectangle2D leftTabRect = getAbsoluteRect(leftTab);
                        Rectangle2D rightTabRect = getAbsoluteRect(rightTab);
                        if(betweenX(leftTabRect, rightTabRect, screenPoint.getX())) {
                            tabInsertIndex = i + 1;
                            break;
                        }
                    }
                }
            }
            return new InsertData(tabInsertIndex, tabPane);
        }
        return null;
    }

    private void detachNewStage(MouseEvent t) {
        final Stage newStage = new Stage();
        final TabPane pane = new TabPane();
        tabPanes.add(pane);
        newStage.setOnHiding(w -> tabPanes.remove(pane));
        getTabPane().getTabs().remove(BktailTab.this);
        pane.getTabs().add(BktailTab.this);
        pane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if(pane.getTabs().isEmpty()) {
                newStage.hide();
            }
        });
        newStage.setScene(new Scene(pane));
        newStage.initStyle(StageStyle.DECORATED);
        newStage.setX(t.getScreenX());
        newStage.setY(t.getScreenY());
        newStage.show();
        pane.requestLayout();
        pane.requestFocus();
    }


    private Rectangle2D getAbsoluteRect(Tab tab) {
        Region node = ((BktailTab) tab).getControl();
        return getAbsoluteRect(node);
    }

    private Rectangle2D getAbsoluteRect(Region node) {
        return new Rectangle2D(
                node.localToScene(node.getLayoutBounds().getMinX(), node.getLayoutBounds().getMinY()).getX() + node.getScene().getWindow().getX(),
                node.localToScene(node.getLayoutBounds().getMinX(), node.getLayoutBounds().getMinY()).getY() + node.getScene().getWindow().getY(),
                node.getWidth(),
                node.getHeight());
    }

    private Region getControl() {
        return control;
    }

    private boolean betweenX(Rectangle2D r1, Rectangle2D r2, double xPoint) {
        double lowerBound = r1.getMinX() + r1.getWidth() / 2;
        double upperBound = r2.getMaxX() - r2.getWidth() / 2;
        return xPoint >= lowerBound && xPoint <= upperBound;
    }

    private static class InsertData {

        private final int index;
        private final TabPane insertPane;

        public InsertData(int index, TabPane insertPane) {
            this.index = index;
            this.insertPane = insertPane;
        }

        public int getIndex() {
            return index;
        }

        public TabPane getInsertPane() {
            return insertPane;
        }

    }
}