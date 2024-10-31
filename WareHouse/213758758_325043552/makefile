all: clean bin/cRace

bin/cRace: bin/main.o bin/WareHouse.o bin/actions.o bin/Volunteer.o bin/Customer.o bin/Order.o
	g++ -o bin/cRace bin/main.o bin/WareHouse.o bin/actions.o bin/Volunteer.o bin/Customer.o bin/Order.o

bin/main.o: src/main.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/main.o src/main.cpp

bin/WareHouse.o: src/WareHouse.cpp include/WareHouse.h
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/WareHouse.o src/WareHouse.cpp

bin/actions.o: src/actions.cpp include/actions.h
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/actions.o src/actions.cpp

bin/Volunteer.o: src/Volunteer.cpp include/Volunteer.h
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Volunteer.o src/Volunteer.cpp

bin/Customer.o: src/Customer.cpp include/Customer.h
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Customer.o src/Customer.cpp

bin/Order.o: src/Order.cpp include/Order.h
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Order.o src/Order.cpp

clean:
	rm -f bin/*