{
  "$schema": "http://json-schema.org/draft-04/schema#",

  "title": "journey",
  "description": "A journey plan from Reittiopas",
  "properties": {
    "date": {
      "type": "string",
      "description": "Date on which this journey is generated"
    },
    "start": {
      "type": "string",
      "description": "Starting point of this journey"
    },
    "dest": {
      "type": "string",
      "description": "Destination of this journey"
    },
    "arrivalTime": {
      "type": "string",
      "description": "Time of arrival at the destination"
    },
    "segments": {
      "type": "array",
      "description": "Segments (lags) of this journey",
      "items": {
        "type": "object",
        "oneOf": [ { "$ref": "#/definitions/routeSegment" } ]
      },
      "required": ["date", "start", "dest", "arrivalTime"]
    }
  },
  "definitions": {
    "waypoint" : {
      "properties": {
        "time": {
          "type": "string",
          "description": "Time of arrival at this waypoint"
        },
        "name": {
          "type": "string",
          "description": "Name of this waypoint"
        },
        "stopCode": {
          "type": "string",
          "description": "HSL stop code for this waypoint"
        }
      },
      "required": ["time", "name"]
    },
    "routeSegment": {
      "properties": {
        "startTime": {
          "type": "string",
          "description": "Start time of this segment"
        },
        "startPoint": {
          "type": "string",
          "description": "Starting point of this segment"
        },
        "mode": {
          "type": "string",
          "description": "Mode of this segment (walk, waiting, bus, train, tram, ferry, ...)"
        },
        "waypoints": {
          "type": "array",
          "description": "Waypoints of this segment",
          "items": {
            "type": "object",
            "oneOf" : [ { "$ref": "#/definitions/waypoint" } ]
          }
        }
      },
      "required": ["startTime", "startPoint", "mode"]
    }
  }
}

