#include "WareHouse.h"
// we add the action to warehouse from the baseaction class

// Warehouse responsible for Volunteers, Customers and Actions.

WareHouse::WareHouse(const string &configFilePath) : isOpen(true), actionsLog(), volunteers(), pendingOrders(), inProcessOrders(), completedOrders(), customers(), customerCounter(-1), volunteerCounter(-1), ordercounter(-1)
{
    readConfigFile(configFilePath);
}

void WareHouse::readConfigFile(const std::string &filePath)
{
    std::ifstream configFile(filePath);

    if (!configFile.is_open())
    {
        std::cerr << "Error: Unable to open configuration file.\n";
        return;
    }

    std::string line;
    while (std::getline(configFile, line))
    {
        // Ignore lines starting with #
        if (line.empty() || line[0] == '#')
        {
            continue;
        }

        std::istringstream iss(line);
        std::string type;
        iss >> type;

        if (type == "customer")
        {
            std::string name, customerType;
            int distance, maxOrders;
            iss >> name >> customerType >> distance >> maxOrders;

            if (customerType == "soldier")
            {
                add_soldier_customer(name, distance, maxOrders);
            }
            else if (customerType == "civilian")
            {
                add_civilian_customer(name, distance, maxOrders);
            }
        }
        else if (type == "volunteer")
        {
            std::string name, volunteerType;
            int cooldown, maxDistance, distancePerStep, maxOrders;

            iss >> name >> volunteerType;

            if (volunteerType == "collector" || volunteerType == "limited_collector")
            {
                iss >> cooldown >> maxOrders;
            }
            else if (volunteerType == "driver" || volunteerType == "limited_driver")
            {
                iss >> maxDistance >> distancePerStep >> maxOrders;
            }

            if (volunteerType == "collector")
            {
                add_collector(name, cooldown);
            }
            else if (volunteerType == "limited_collector")
            {
                add_limited_collector(name, cooldown, maxOrders);
            }
            else if (volunteerType == "driver")
            {
                add_driver(name, maxDistance, distancePerStep);
            }
            else if (volunteerType == "limited_driver")
            {
                add_limited_driver(name, maxDistance, distancePerStep, maxOrders);
            }
        }
        // Additional parsing based on your configuration file structure...
    }
}

void WareHouse::start()
{

    std::cout << "warehouse is open:" << std::endl;
    isOpen = true;

    std::string userInput;

    while (isOpen)
    {
        // Prompt the user for input
        std::cout << "Enter a command: ";
        std::getline(std::cin, userInput);

        // Convert the input to lowercase for case-insensitivity
        std::transform(userInput.begin(), userInput.end(), userInput.begin(), ::tolower);

        // Tokenize the input to extract command and arguments
        std::istringstream iss(userInput);
        std::string command;
        iss >> command;

        if (command == "order")
        {
            // Extract the order ID from the input
            int orderId;
            iss >> orderId;
            // Call the AddOrder function
            AddOrder input = AddOrder(orderId);
            input.act((*this));
        }
        else if (command == "orderstatus")
        {
            // Extract the order ID from the input
            int orderId;
            iss >> orderId;

            // Call the PrintOrderStatus function
            PrintOrderStatus input = PrintOrderStatus(orderId);
            input.act((*this));
        }
        else if (command == "step")
        {
            // Extract the number of steps from the input
            int numOfSteps;
            iss >> numOfSteps;

            // Call the SimulateStep function
            SimulateStep input = SimulateStep(numOfSteps);
            input.act((*this));
        }
        else if (command == "volunteerstatus")
        {
            // Extract the volunteer ID from the input
            int volunteerId;
            iss >> volunteerId;

            // Call the PrintVolunteerStatus function
            PrintVolunteerStatus input = PrintVolunteerStatus(volunteerId);
            input.act((*this));
        }
        else if (command == "log")
        {
            // Call the PrintActionsLog function
            PrintActionsLog input = PrintActionsLog();
            input.act((*this));
        }
        else if (command == "close")
        {
            // Call the Close function
            Close input = Close();
            input.act((*this));
        }
        else if (command == "backup")
        {
            // Call the BackupWareHouse function
            BackupWareHouse input = BackupWareHouse();
            input.act((*this));
        }
        else if (command == "restore")
        {
            // Call the RestoreWareHouse function
            RestoreWareHouse input = RestoreWareHouse();
            input.act((*this));
        }
        else if (command == "customerstatus")
        {
            // Extract the customer ID from the input
            int customerId;
            iss >> customerId;

            // Call the PrintCustomerStatus function
            PrintCustomerStatus input = PrintCustomerStatus(customerId);
            input.act((*this));
        }
        else if (command == "customer")
        {
            // Extract customer information from the input
            std::string customerName, customerType;
            int distance, maxOrders;
            iss >> customerName >> customerType >> distance >> maxOrders;

            // Call the AddCustomer function
            AddCustomer input = AddCustomer(customerName, customerType, distance, maxOrders);
            input.act((*this));
        }
        else
        {
            std::cout << "Invalid command. Please try again." << std::endl;
        }
    }
}

const vector<actions *> &WareHouse::getActions() const
{
    return actionsLog;
}

void WareHouse::addAction(actions *action)
{
    actionsLog.push_back(action);
}

bool WareHouse::WH_AddOrder(int customerId)
{

    if (customerId > customerCounter || customerId < 0)
    {
        return false;
    }
    if (!(getCustomer(customerId)).canMakeOrder())
    {
        return false;
    }
    int c = get_order_counter();
    int i = customerId;
    int d = getCustomer(customerId).getCustomerDistance();
    Order *input = new Order(c, i, d);
    pendingOrders.push_back(input);
    getCustomer(customerId).addOrder(c);

    // add the order Id the vector<int>

    return true;
}

bool WareHouse::WH_AddCustomer(string customerName, string customerType, int distance, int maxOrders)
{
    int i = get_customer_counter();
    Customer *input;

    if (customerType == "soldier")
        input = new SoldierCustomer(i, customerName, distance, maxOrders);
    else
        input = new CivilianCustomer(i, customerName, distance, maxOrders);

    addcustomer_warehouse(input);
    return true;
}

bool WareHouse::WH_PrintOrderStatus(int orderid)
{
    if (ordercounter < orderid || orderid < 0)
        return false;

    Order ord = getOrder(orderid);
    std::string ordercollecter;
    std::string orderdriver;

    if (ord.getCollectorId() == -1)
        ordercollecter = "None";
    else
        ordercollecter = std::to_string(ord.getCollectorId());

    if (ord.getDriverId() == -1)
        orderdriver = "None";
    else
        orderdriver = std::to_string(ord.getDriverId());

    std::cout << "OrderId : " + std::to_string(orderid) << std::endl;
    std::cout << "OrderStatus : " + ord.statusToString(ord.getStatus()) << std::endl;
    std::cout << "CustomerId : " + std::to_string(ord.getCustomerId()) << std::endl;
    std::cout << "CollectorId: " + ordercollecter << std::endl;
    std::cout << "DriverId: " + orderdriver << std::endl;

    return true;
}

bool WareHouse::WH_PrintCustomerStatus(int customerId)
{
    if (customerCounter < customerId || customerId < 0)
        return false;

    vector<int> ordersId = getCustomer(customerId).getOrdersIds();
    string status;

    std::cout << "Customer ID: " + std::to_string(customerId) << std::endl;

    for (int i : ordersId)
    {
        status = (getOrder(i)).statusToString((getOrder(i).getStatus()));
        std::cout << "orderid: " << i << std::endl;
        std::cout << "Order status: " + status << std::endl;
    }
    std::cout << "numOrdersLeft " + to_string(getCustomer(customerId).getMaxOrders() - ordersId.size()) << std::endl;

    return true;
}

Customer &WareHouse::getCustomer(int customerId) const
{
    for (size_t i = 0; i < customers.size(); i++)
    {
        Customer *c = customers[i];
        if (c->getId() == customerId)
        {
            return *c;
        }
    }
    throw std::runtime_error("");
}

Volunteer &WareHouse::getVolunteer(int volunteerId) const // this function return the volunteer
{
    for (size_t i = 0; i < volunteers.size(); i++)
    {
        Volunteer *v = (volunteers[i]);
        if (v->getId() == volunteerId)
            return *v;
    }
    throw std::runtime_error("");
}

bool WareHouse::WH_PrintVolunteerStatus(int VolunteerId)
{
    Volunteer *vol = nullptr; // Declare the variable outside the try block

    try
    {
        vol = &(getVolunteer(VolunteerId));
    }
    catch (const std::runtime_error &e)
    {
        std::cerr << "" << e.what() << std::endl;
    }

    if (vol != nullptr)
    {
        std::cout << vol->toString() << std::endl;
        return true;
    }

    return false;
}

void WareHouse::WH_step()
{
    bool accepted = false;

    vector<Order *> newpendingorders;

    for (size_t i = 0; i < pendingOrders.size(); i++) // shlav 1
    {

        accepted = false;

        if (pendingOrders[i]->getStatus() == OrderStatus::PENDING)
        {

            for (size_t i = 0; !accepted && i < volunteers.size(); i++)
            {

                if (!volunteers[i]->isBusy() && volunteers[i]->getrole() <= 2) // shalv 1
                {

                    volunteers[i]->acceptOrder(*pendingOrders[i]);

                    pendingOrders[i]->setCollectorId(volunteers[i]->getId());

                    pendingOrders[i]->setStatus(OrderStatus::COLLECTING);

                    inProcessOrders.push_back(pendingOrders[i]->clone());

                    accepted = true;
                }
            }
            if (!accepted)
            {
                newpendingorders.push_back(pendingOrders[i]->clone());
            }
        }
        else
        {

            for (size_t j = 0; j < volunteers.size() && !accepted; j++)
            {

                if (volunteers[j]->canTakeOrder(*pendingOrders[i]) && 2 < volunteers[j]->getrole())
                {

                    volunteers[j]->acceptOrder(*pendingOrders[i]);
                    pendingOrders[i]->setDriverId(volunteers[j]->getId());
                    pendingOrders[i]->setStatus(OrderStatus::DELIVERING);
                    inProcessOrders.push_back(pendingOrders[i]->clone());
                    accepted = true;
                }
            }
            if (!accepted)
            {

                newpendingorders.push_back(pendingOrders[i]->clone());
            }
        }
    }

    for (Order *ord : pendingOrders)
        delete (ord);
    pendingOrders.clear();
    pendingOrders = newpendingorders;

    for (Volunteer *v : volunteers) // shlav 2
        (*v).step();

    vector<Order *> NewInProccesOrderd;
    accepted = false;

    for (Order *ord : inProcessOrders) // shlav 3 + 4
    {
        accepted = false;

        if (ord->getStatus() == OrderStatus::COLLECTING) // if the order COLLECTING
        {

            Volunteer *v = &(getVolunteer(ord->getCollectorId()));

            CollectorVolunteer *koko = dynamic_cast<CollectorVolunteer *>(v);
            if (koko->getTimeLeft() == 0)
            {
                accepted = true;

                pendingOrders.push_back(ord->clone());

                koko->set_completedOrderId(ord->getId());

                koko->set_activeOrderId(-1);

                if (koko->getrole() == 2 && (!koko->hasOrdersLeft()))
                {
                    WareHouse::delete_volunteer(koko->getId());
                }
            }
            if (!accepted)
                NewInProccesOrderd.push_back(ord->clone());
        }

        else // if the order DELIVIRNG
        {
            Volunteer *v = &(getVolunteer(ord->getDriverId()));

            DriverVolunteer *koko = dynamic_cast<DriverVolunteer *>(v);

            if (koko->getDistanceLeft() == 0)
            {
                ord->setStatus(OrderStatus::COMPLETED);

                completedOrders.push_back(ord->clone());

                koko->set_completedOrderId(ord->getId());

                koko->set_activeOrderId(-1);

                accepted = true;

                if (koko->getrole() == 4 && (!koko->hasOrdersLeft()))
                {

                    delete_volunteer(koko->getId());
                }
            }
            if (!accepted)
                NewInProccesOrderd.push_back(ord->clone());
        }
    }
    for (Order *ord : inProcessOrders)
        delete (ord);
    inProcessOrders.clear();
    inProcessOrders = NewInProccesOrderd;
}

void WareHouse::delete_volunteer(int id)
{
    for (size_t i = 0; i < volunteers.size(); i++)
    {
        if (volunteers[i]->getId() == id)
        {
            delete (volunteers[i]);
            volunteers.erase(volunteers.begin() + i);
        }
    }
}

Order &WareHouse::getOrder(int orderId) const
{
    for (Order *v : pendingOrders)
        if (v->getId() == orderId)
            return (*v);
    for (Order *v : inProcessOrders)
        if (v->getId() == orderId)
            return (*v);
    for (Order *v : completedOrders)
        if (v->getId() == orderId)
            return (*v);

    throw std::runtime_error("");
}
int WareHouse::create_customer_id()
{
    customerCounter = customerCounter + 1;
    return customerCounter;
}
int WareHouse::create_order_id()
{
    ordercounter = ordercounter + 1;
    return ordercounter;
}
int WareHouse::create_volunteer_id()
{
    volunteerCounter = volunteerCounter + 1;
    return volunteerCounter;
}
void WareHouse::addcustomer_warehouse(Customer *customer)
{
    customers.push_back(customer);
}

int WareHouse::get_customer_counter()
{
    customerCounter = customerCounter + 1;
    return customerCounter;
}
int WareHouse::get_order_counter()
{

    ordercounter = ordercounter + 1;
    return ordercounter;
}

void WareHouse::close()
{
    for (size_t i = 0; i < pendingOrders.size(); i++)
    { // print to string
        std::cout << pendingOrders[i]->toString() << std::endl;
    }

    for (size_t i = 0; i < inProcessOrders.size(); i++)
    { // print to string
        std::cout << inProcessOrders[i]->toString() << std::endl;
    }
    for (size_t i = 0; i < completedOrders.size(); i++)
    { // print to string
        std::cout << completedOrders[i]->toString() << std::endl;
    }

    for (actions *action : actionsLog)
    {
        delete action;
    }
    actionsLog.clear();

    for (Volunteer *volunteer : volunteers)
    {
        delete volunteer;
    }
    volunteers.clear();

    for (Order *order : pendingOrders)
    {
        delete order;
    }
    pendingOrders.clear();

    for (Order *order : inProcessOrders)
    {
        delete order;
    }
    inProcessOrders.clear();

    for (Order *order : completedOrders)
    {
        delete order;
    }
    completedOrders.clear();

    for (Customer *customer : customers)
    {
        delete customer;
    }
    customers.clear();

    isOpen = false;
}

void WareHouse::open() { isOpen = true; }

WareHouse &WareHouse::operator=(const WareHouse &other)
{
    // Check for self-assignment
    if (this != &other)
    {
        // Copy non-pointer members
        isOpen = other.isOpen;
        customerCounter = other.customerCounter;
        volunteerCounter = other.volunteerCounter;
        ordercounter = other.ordercounter;

        // Clear existing data
        for (actions *action : actionsLog)
        {
            delete action;
        }
        actionsLog.clear();

        for (Volunteer *volunteer : volunteers)
        {
            delete volunteer;
        }
        volunteers.clear();

        for (Order *order : pendingOrders)
        {
            delete order;
        }
        pendingOrders.clear();

        for (Order *order : inProcessOrders)
        {
            delete order;
        }
        inProcessOrders.clear();

        for (Order *order : completedOrders)
        {
            delete order;
        }
        completedOrders.clear();

        for (Customer *customer : customers)
        {
            delete customer;
        }
        customers.clear();

        for (const auto *order : other.pendingOrders)
        {
            pendingOrders.push_back(new Order(*order));
        }

        for (const Volunteer *volunteer : other.volunteers)
        {
            volunteers.push_back(volunteer->clone());
        }

        for (const Order *order : other.inProcessOrders)
        {
            inProcessOrders.push_back(new Order(*order));
        }

        for (const Order *order : other.completedOrders)
        {
            completedOrders.push_back(new Order(*order));
        }

        for (const Customer *customer : other.customers)
        {
            customers.push_back(customer->clone());
        }
        for (const actions *action : other.actionsLog)
        {
            actionsLog.push_back(action->clone());
        }
    }

    return *this; // Return a reference to the modified object
}

WareHouse::~WareHouse()
{

    for (actions *action : actionsLog)
    {
        delete action;
    }
    actionsLog.clear();

    // Clean up volunteers
    for (Volunteer *volunteer : volunteers)
    {
        delete volunteer;
    }
    volunteers.clear();

    // Clean up pendingOrders
    for (Order *order : pendingOrders)
    {
        delete order;
    }
    pendingOrders.clear();

    // Clean up inProcessOrders
    for (Order *order : inProcessOrders)
    {
        delete order;
    }
    inProcessOrders.clear();

    // Clean up completedOrders
    for (Order *order : completedOrders)
    {
        delete order;
    }
    completedOrders.clear();

    // Clean up customers
    for (Customer *customer : customers)
    {
        delete customer;
    }
    customers.clear();
}

WareHouse::WareHouse(const WareHouse &other) : isOpen(other.isOpen), actionsLog(), volunteers(), pendingOrders(), inProcessOrders(), completedOrders(), customers(), customerCounter(other.customerCounter), volunteerCounter(other.volunteerCounter), ordercounter(other.ordercounter)
{
    if (this != &other)
    {

        for (size_t i = 0; i < other.actionsLog.size(); i++)
        {
            actionsLog.push_back(other.actionsLog[i]->clone());
        }

        for (size_t i = 0; i < other.volunteers.size(); i++)
            volunteers.push_back(other.volunteers[i]->clone());

        for (size_t i = 0; i < other.pendingOrders.size(); i++)
            pendingOrders.push_back(other.pendingOrders[i]->clone());

        for (size_t i = 0; i < other.inProcessOrders.size(); i++)
            inProcessOrders.push_back(other.inProcessOrders[i]->clone());

        for (size_t i = 0; i < other.completedOrders.size(); i++)
            completedOrders.push_back(other.completedOrders[i]->clone());

        for (size_t i = 0; i < other.customers.size(); i++)
            customers.push_back(other.customers[i]->clone());
    }
}

void WareHouse::add_collector(std::string name, int cooldown)
{
    CollectorVolunteer *input = new CollectorVolunteer(create_volunteer_id(), name, cooldown);
    volunteers.push_back(input);
}

void WareHouse::add_limited_collector(std::string name, int cooldown, int maxOrders)
{
    LimitedCollectorVolunteer *input = new LimitedCollectorVolunteer(create_volunteer_id(), name, cooldown, maxOrders);
    volunteers.push_back(input);
}

void WareHouse::add_driver(std::string name, int maxdistance, int distancePerStep)
{

    DriverVolunteer *input = new DriverVolunteer(create_volunteer_id(), name, maxdistance, distancePerStep);
    volunteers.push_back(input);
}

void WareHouse::add_limited_driver(std::string name, int maxdistance, int distancePerStep, int maxOrders)
{
    LimitedDriverVolunteer *input = new LimitedDriverVolunteer(create_volunteer_id(), name, maxdistance, distancePerStep, maxOrders);
    volunteers.push_back(input);
}

void WareHouse::add_soldier_customer(std::string name, int distance, int maxOrders)
{
    SoldierCustomer *input = new SoldierCustomer(create_customer_id(), name, distance, maxOrders);
    customers.push_back(input);
}

void WareHouse::add_civilian_customer(std::string name, int distance, int maxOrders)
{
    CivilianCustomer *input = new CivilianCustomer(create_customer_id(), name, distance, maxOrders);
    customers.push_back(input);
}

void WareHouse::addOrder(Order *order)
{
    pendingOrders.push_back(order);
};
const vector<Volunteer *> &WareHouse ::getvolunteers() const
{ // just for test

    return volunteers;
}
WareHouse *WareHouse ::WH_GiveMeCopy() const
{
    WareHouse *temp = new WareHouse(*this);
    return temp;
}
WareHouse::WareHouse(WareHouse &&other) noexcept
    : isOpen(other.isOpen),
      actionsLog(std::move(other.actionsLog)),
      volunteers(std::move(other.volunteers)),
      pendingOrders(std::move(other.pendingOrders)),
      inProcessOrders(std::move(other.inProcessOrders)),
      completedOrders(std::move(other.completedOrders)),
      customers(std::move(other.customers)),
      customerCounter(other.customerCounter),
      volunteerCounter(other.volunteerCounter),
      ordercounter(other.ordercounter)
{
    other.isOpen = false;
    other.customerCounter = 0;
    other.volunteerCounter = 0;
    other.ordercounter = 0;
}

WareHouse &WareHouse::operator=(WareHouse &&other) noexcept
{
    if (this != &other)
    {
        isOpen = other.isOpen;
        customerCounter = other.customerCounter;
        volunteerCounter = other.volunteerCounter;
        ordercounter = other.ordercounter;

        other.isOpen = false;
        other.customerCounter = 0;
        other.volunteerCounter = 0;
        other.ordercounter = 0;

        volunteers = std::move(other.volunteers);
        customers = std::move(other.customers);
        actionsLog = std::move(other.actionsLog);
        pendingOrders = std::move(other.pendingOrders);
        inProcessOrders = std::move(other.inProcessOrders);
        completedOrders = std::move(other.completedOrders);
    }
    return *this;
}