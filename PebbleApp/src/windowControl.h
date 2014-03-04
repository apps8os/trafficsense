#ifndef __WIN_CNTL_H__
#define __WIN_CNTL_H__

#include "menu.h"
#include "main.h"

#define NUM_WINDOWS			2
#define WINDOW_BASIC		0
#define WINDOW_3STOP		1

#define NUM_VIEW_MODES		2
#define VIEW_MODE_NAMES		0
#define VIEW_MODE_CODES		1

#define NUM_TIME_UNITS		2

static const char* timeUnitsStr[NUM_TIME_UNITS] = {"seconds", "minutes"};

extern int viewMode;
extern Window* windowArray[NUM_WINDOWS];

void init_windows();
// Called when the MIDDLE button is clicked once.
void stoplist_window_single_click_SELECT_handler(ClickRecognizerRef recognizer, void* context);
// Function for setting callbacks for button clicks.
void click_config_provider(Window *window);
// Pop up the get off alarm window and vibrate
void show_get_off_alarm();

#endif
