CREATE TABLE IF NOT EXISTS Coordinate (
    UUID VARCHAR(36) PRIMARY KEY,
    X INTEGER NOT NULL,
    Y INTEGER NOT NULL,
    Z INTEGER NOT NULL,
    WORLD_NAME VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS Coordinate_Users (
    PLAYER_UUID VARCHAR(36),
    COORDINATE_UUID INTEGER,
    NAME VARCHAR(255) NOT NULL,

    FOREIGN KEY (COORDINATE_UUID) REFERENCES Coordinate(UUID),
    PRIMARY KEY(PLAYER_UUID, COORDINATE_UUID)
);