#include <pebble.h>

#define NUM_STOPS		3	// Number of stops shown in the pebble screen, including the last stop
#define NUM_VIEW_MODES		2
#define VIEW_MODE_NAMES		0
#define VIEW_MODE_CODES		1
// Constants for receiving commands
#define KEY_COMMAND		0	// The key of the command tuple (the value of this tuple is the command)
// Constants related to the command for receiving a stop
#define COMMAND_GET_STOP	0
#define KEY_STOP_NUM		1	// The value of a tuple with this key defines where the stop is in the list	
#define KEY_STOP_NAME		2
#define KEY_STOP_CODE		3
#define KEY_STOP_TIME		4
#define STOP_NAME_LENGTH	21
#define STOP_CODE_LENGTH	7
#define TIME_STR_LENGTH		6

struct Stop {
	char name[STOP_NAME_LENGTH];
	char code[STOP_CODE_LENGTH];
	char time[TIME_STR_LENGTH];
};
typedef struct Stop Stop;

extern Stop stopArray[NUM_STOPS];

uint16_t menu_get_num_sections_callback(MenuLayer *menu_layer, void *data);
uint16_t menu_get_num_rows_callback(MenuLayer *menu_layer, uint16_t section_index, void *data);
int16_t menu_get_header_height_callback(MenuLayer *menu_layer, uint16_t section_index, void *data);
void menu_draw_header_callback(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data);
void menu_draw_row_callback(GContext* ctx, const Layer *cell_layer, MenuIndex *cell_index, void *data);
// Called when the user selects a menu item
void menu_select_callback(MenuLayer *menu_layer, MenuIndex *cell_index, void *data);
// Automatically called when a message is received from the phone.
void message_received(DictionaryIterator *iterator);
// Sends the value cmd to the phone.
void send_cmd(uint8_t cmd);
// Called when the UP button is clicked once.
void stoplist_window_single_click_UP_handler(ClickRecognizerRef recognizer, void* context);
// Function for setting callbacks for button clicks.
void stoplist_window_click_config_provider(Window *window);
// Initializes the app, called when the app is started, in main()
void init_menu();
void init(void);
void deinit(void);
int main(void);
