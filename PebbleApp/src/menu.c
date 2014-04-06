#include "common.h"

MenuLayer *menu_layer;

/**
 * TODO: documentation.
 */
uint16_t menu_get_num_sections_callback(MenuLayer *menu_layer, void *data) {
  return 2;
}

/**
 * TODO: documentation.
 */
uint16_t menu_get_num_rows_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
  switch (section_index) {
    case 0:
      return NUM_STOPS - 1;

    case 1:
      return 1;

    default:
      return 0;
  }
}

/**
 * TODO: documentation.
 */
int16_t menu_get_header_height_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
  switch (section_index) {
    // Section 0: Upcoming stops, return 0 not to show this header at all
    case 0:
      return 0;
    // Section 1: Last stop, return the default height to show the header
    case 1:
      return MENU_CELL_BASIC_HEADER_HEIGHT;
  }
  // Return 0 by default, if the section index is out of range for some reason
  return 0;
}

/**
 * TODO: documentation.
 */
void menu_draw_header_callback(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
  // Determine which section we're working with
  switch (section_index) {
    case 0:
      // Draw title text in the section header
      menu_cell_basic_header_draw(ctx, cell_layer, "Next stops");
      break;

    case 1:
      menu_cell_basic_header_draw(ctx, cell_layer, "Last stop");
      break;
  }
}

/**
 * TODO: documentation.
 */
void menu_draw_row_callback(GContext* ctx, const Layer *cell_layer, MenuIndex *cell_index, void *data) {
  // Determine which section we're going to draw in
  int row = cell_index->row;
  int section = cell_index->section;
  // Use the values of row and section to determine which item we'll draw
  int index = (NUM_STOPS-1)*section + row;
  if (viewMode == VIEW_MODE_NAMES)
    menu_cell_basic_draw(ctx, cell_layer, stopArray[index].name, stopArray[index].time, NULL);
  else if (viewMode == VIEW_MODE_CODES)
    menu_cell_basic_draw(ctx, cell_layer, stopArray[index].code, stopArray[index].time, NULL);
}

/**
 * Called when the user selects a menu item.
 */
void menu_select_callback(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
  // Do nothing
}

void init_menu(Window* window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_frame(window_layer);
  menu_layer = menu_layer_create(bounds);

  menu_layer_set_callbacks(menu_layer, NULL, (MenuLayerCallbacks){
    .get_num_sections = menu_get_num_sections_callback,
    .get_num_rows = menu_get_num_rows_callback,
    .get_header_height = menu_get_header_height_callback,
    .draw_header = menu_draw_header_callback,
    .draw_row = menu_draw_row_callback,
    .select_click = menu_select_callback,
  });

  //menu_layer_set_click_config_onto_window(menu_layer, window);
  layer_add_child(window_layer, menu_layer_get_layer(menu_layer));
}
