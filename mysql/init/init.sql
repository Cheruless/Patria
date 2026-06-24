CREATE DATABASE IF NOT EXISTS ms_scrap_diputados;
CREATE DATABASE IF NOT EXISTS ms_scrap_locations;
CREATE DATABASE IF NOT EXISTS ms_profile;

CREATE USER 'user_scrap_diputados'@'%' IDENTIFIED BY 'ms.SDiputados.1';
GRANT ALL PRIVILEGES ON ms_scrap_diputados.* TO 'user_scrap_diputados'@'%';


CREATE USER 'user_scrap_locations'@'%' IDENTIFIED BY 'ms.SLocations.1';
GRANT ALL PRIVILEGES ON ms_scrap_locations.* TO 'user_scrap_locations'@'%';


CREATE USER 'user_profiles'@'%' IDENTIFIED BY 'ms.Profiles.1';
GRANT ALL PRIVILEGES ON ms_profile.* TO 'user_profiles'@'%';