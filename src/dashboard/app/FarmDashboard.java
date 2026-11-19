package dashboard.app;

import dashboard.model.FarmComponent;
import dashboard.model.Item;
import dashboard.model.ItemContainer;
import dashboard.ui.FarmVisualizer;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class FarmDashboard {

    private static FarmDashboard instance;

    private ItemContainer farmRoot;
    private FarmVisualizer visualizer;
    private TreeView<FarmComponent> treeView;
    private Label statusLabel;
    private Stage dashboardStage;
    private Scene scene;

    private FarmDashboard() {
        farmRoot = new ItemContainer("Farm", 0, 0, 0, 0, 0, 0);
    }

    public static FarmDashboard getInstance() {
        if (instance == null) {
            instance = new FarmDashboard();
        }
        return instance;
    }

    public void showDashboard(Stage stage) {
        if (dashboardStage == null) {
            dashboardStage = stage;
        }

        if (scene == null) {
            createDashboard();
        }

        dashboardStage.setTitle("Farm Dashboard");
        dashboardStage.setScene(scene);
        dashboardStage.show();
    }

    private void createDashboard() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label title = new Label("Farm Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        layout.setTop(title);

        treeView = new TreeView<>();
        treeView.setPrefSize(300, 450);

        Button addItem = new Button("Add Item");
        Button addContainer = new Button("Add Container");
        Button edit = new Button("Edit");
        Button delete = new Button("Delete");

        addItem.setOnAction(e -> addComponent(false));
        addContainer.setOnAction(e -> addComponent(true));
        edit.setOnAction(e -> editSelected());
        delete.setOnAction(e -> deleteSelected());

        VBox leftPanel = new VBox(
            10,
            new Label("Items / Item Containers"),
            treeView,
            new HBox(10, addItem, addContainer),
            new HBox(10, edit, delete)
        );

        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(330);
        layout.setLeft(leftPanel);

        Pane farmPane = new Pane();
        farmPane.setPrefSize(800, 600);
        farmPane.setMinSize(800, 600);
        farmPane.setMaxSize(800, 600);
        farmPane.setStyle(
            "-fx-background-color: white; -fx-border-color: black;"
        );

        visualizer = new FarmVisualizer(farmPane);

        VBox centerPanel = new VBox(
            10,
            new Label("Farm Visualization (800 x 600)"),
            new ScrollPane(farmPane)
        );

        centerPanel.setPadding(new Insets(10));
        layout.setCenter(centerPanel);

        Button visit = new Button("Go to Item");
        Button scan = new Button("Scan Farm");

        visit.setDisable(true);
        scan.setDisable(true);

        statusLabel = new Label("Farm Dashboard Ready");

        VBox bottomPanel = new VBox(
            10,
            new HBox(15, visit, scan),
            statusLabel
        );

        bottomPanel.setPadding(new Insets(15));
        layout.setBottom(bottomPanel);

        refreshDashboard();

        scene = new Scene(layout, 1250, 800);
    }

    private void addComponent(boolean isContainer) {
        TreeItem<FarmComponent> selected =
            treeView.getSelectionModel().getSelectedItem();

        if (selected == null ||
            !(selected.getValue() instanceof ItemContainer)) {
            showMessage("Please select a container first.");
            return;
        }

        FarmComponent component = showForm(null, isContainer);

        if (component != null) {
            ItemContainer parent =
                (ItemContainer) selected.getValue();

            parent.add(component);
            refreshDashboard();
            statusLabel.setText(component.getName() + " added.");
        }
    }

    private void editSelected() {
        TreeItem<FarmComponent> selected =
            treeView.getSelectionModel().getSelectedItem();

        if (selected == null || selected.getValue() == farmRoot) {
            showMessage("Please select an item to edit.");
            return;
        }

        FarmComponent component = selected.getValue();

        if (showForm(component, component instanceof ItemContainer) != null) {
            refreshDashboard();
            statusLabel.setText("Item updated.");
        }
    }

    private void deleteSelected() {
        TreeItem<FarmComponent> selected =
            treeView.getSelectionModel().getSelectedItem();

        if (selected == null || selected.getValue() == farmRoot) {
            showMessage("Please select an item to delete.");
            return;
        }

        ItemContainer parent =
            (ItemContainer) selected.getParent().getValue();

        parent.remove(selected.getValue());

        refreshDashboard();
        statusLabel.setText("Item deleted.");
    }

    private FarmComponent showForm(
        FarmComponent existing, boolean isContainer
    ) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Object" : "Edit Object");

        dialog.getDialogPane().getButtonTypes().addAll(
            ButtonType.OK, ButtonType.CANCEL
        );

        String[] labels = {
            "Name", "Price", "Location X", "Location Y",
            "Length", "Width", "Height"
        };

        TextField[] fields = new TextField[7];

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        for (int i = 0; i < fields.length; i++) {
            fields[i] = new TextField();
            grid.addRow(i, new Label(labels[i] + ":"), fields[i]);
        }

        if (existing != null) {
            fields[0].setText(existing.getName());
            fields[1].setText("" + existing.getPrice());
            fields[2].setText("" + existing.getLocationX());
            fields[3].setText("" + existing.getLocationY());
            fields[4].setText("" + existing.getLength());
            fields[5].setText("" + existing.getWidth());
            fields[6].setText("" + existing.getHeight());
        } else {
            String[] defaults = {
                "", "0", "40", "40", "80", "60", "10"
            };

            for (int i = 0; i < fields.length; i++) {
                fields[i].setText(defaults[i]);
            }
        }

        dialog.getDialogPane().setContent(grid);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL)
            != ButtonType.OK) {
            return null;
        }

        try {
            String name = fields[0].getText().trim();
            double[] values = new double[6];

            for (int i = 0; i < values.length; i++) {
                values[i] = Double.parseDouble(
                    fields[i + 1].getText()
                );

                if (!Double.isFinite(values[i])) {
                    showMessage("Please enter valid numbers.");
                    return null;
                }
            }

            double price = values[0];
            double x = values[1];
            double y = values[2];
            double length = values[3];
            double width = values[4];
            double height = values[5];

            if (name.isEmpty() || price < 0 ||
                x < 0 || y < 0 ||
                length <= 0 || width <= 0 || height <= 0 ||
                x + length > 800 || y + width > 600) {
                showMessage("Enter valid values within the 800 x 600 farm.");
                return null;
            }

            if (existing != null) {
                existing.setName(name);
                existing.setPrice(price);
                existing.setLocationX(x);
                existing.setLocationY(y);
                existing.setLength(length);
                existing.setWidth(width);
                existing.setHeight(height);
                return existing;
            }

            if (isContainer) {
                return new ItemContainer(
                    name, price, x, y, length, width, height
                );
            }

            return new Item(
                name, price, x, y, length, width, height
            );

        } catch (NumberFormatException e) {
            showMessage("Please enter valid numbers.");
            return null;
        }
    }

    private void refreshDashboard() {
        TreeItem<FarmComponent> root = createTreeItem(farmRoot);
        treeView.setRoot(root);
        treeView.getSelectionModel().select(root);
        visualizer.redraw(farmRoot);
    }

    private TreeItem<FarmComponent> createTreeItem(
        FarmComponent component
    ) {
        TreeItem<FarmComponent> item = new TreeItem<>(component);
        item.setExpanded(true);

        for (FarmComponent child : component.getChildren()) {
            item.getChildren().add(createTreeItem(child));
        }

        return item;
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Farm Dashboard");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}