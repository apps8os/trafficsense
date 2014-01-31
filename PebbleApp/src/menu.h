#ifndef __MENU_H__
#define __MENU_H__

#include "main.h"

// Here we capture when a user selects a menu item
extern MenuLayer *menu_layer;

uint16_t menu_get_num_sections_callback(MenuLayer *menu_layer, void *data);
uint16_t menu_get_num_rows_callback(MenuLayer *menu_layer, uint16_t section_index, void *data);
int16_t menu_get_header_height_callback(MenuLayer *menu_layer, uint16_t section_index, void *data);
void menu_draw_header_callback(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data);
void menu_draw_row_callback(GContext* ctx, const Layer *cell_layer, MenuIndex *cell_index, void *data);
// Called when the user selects a menu item
void menu_select_callback(MenuLayer *menu_layer, MenuIndex *cell_index, void *data);
// Initialize the menu for the given window
void init_menu(Window* window);
#endif
