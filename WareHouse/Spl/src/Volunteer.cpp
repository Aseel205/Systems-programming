#include "Volunteer.h"
#include <string>
#include <vector>
#include "Order.h"
using std::string;
using std::vector;
#include <iostream>

#define NO_ORDER -1

Volunteer::Volunteer(int id, const string &name) : role(0), completedOrderId(NO_ORDER), activeOrderId(NO_ORDER), id(id), name(name){};

int Volunteer::getId() const
{
    return id;
}
const string &Volunteer::getName() const
{
    return name;
}
int Volunteer::getActiveOrderId() const
{
    return activeOrderId;
}
int Volunteer::getCompletedOrderId() const
{
    return completedOrderId;
}
int Volunteer::getrole() const
{
    return role;
}
void Volunteer::set_completedOrderId(int orderId)
{
    completedOrderId = orderId;
}
void Volunteer::set_activeOrderId(int i)
{
    activeOrderId = i;
}

bool Volunteer::isBusy() const
{
    return activeOrderId != -1;
}

CollectorVolunteer::CollectorVolunteer(int id, const string &name, int coolDown)
    : Volunteer(id, name), coolDown(coolDown), timeLeft(NO_ORDER)
{
    completedOrderId = NO_ORDER;
    activeOrderId = NO_ORDER;
    role = 1;
}

CollectorVolunteer *CollectorVolunteer::clone() const
{
    CollectorVolunteer *output = new CollectorVolunteer(*this);
    (*output).role = 1;
    return output;
}

int CollectorVolunteer::getCoolDown() const
{
    return coolDown;
}
int CollectorVolunteer::getTimeLeft() const
{
    return timeLeft;
}
bool CollectorVolunteer::decreaseCoolDown()
{
    return timeLeft == 0;
}

void CollectorVolunteer::set_timeleft(int number)
{
    timeLeft = number;
}

void CollectorVolunteer::step()
{
    if (activeOrderId != -1)

        timeLeft--;
}

void CollectorVolunteer::acceptOrder(const Order &order)
{
    activeOrderId = order.getId();
    timeLeft = coolDown;
}

string CollectorVolunteer::toString() const
{
    std::string orderid;
    std::string timeleft;
    if (timeLeft == 0)
        timeleft = "None";
    else
        timeleft = std::to_string(timeLeft);

    if (activeOrderId == NO_ORDER)
    {
        orderid = "None";
    }
    else
    {
        orderid = std::to_string(activeOrderId);
    }
    std::string busyString = (isBusy()) ? "true" : "false";

    std::string output = "volunteer id: " + std::to_string(getId()) + "\nisBusy: " + busyString +
                         "\n order id: " + orderid + "\n time left: " + timeleft + "\n orders left: NO Limit";
    return output;
}

bool CollectorVolunteer::canTakeOrder(const Order &order) const
{
    return !isBusy();
}

bool CollectorVolunteer::hasOrdersLeft() const
{
    return true;
}

LimitedCollectorVolunteer::LimitedCollectorVolunteer(int id, const string &name, int coolDown, int maxOrders)
    : CollectorVolunteer(id, name, coolDown), maxOrders(maxOrders), ordersLeft(maxOrders)
{
    completedOrderId = NO_ORDER;
    activeOrderId = NO_ORDER;
    role = 2;
}

int LimitedCollectorVolunteer::getMaxOrders() const
{
    return maxOrders;
}
int LimitedCollectorVolunteer::getNumOrdersLeft() const
{
    return ordersLeft;
}

void LimitedCollectorVolunteer::decrease_ordersLeft()
{
    ordersLeft = ordersLeft - 1;
}

LimitedCollectorVolunteer *LimitedCollectorVolunteer::clone() const
{
    LimitedCollectorVolunteer *output = new LimitedCollectorVolunteer(*this);
    return output;
}

bool LimitedCollectorVolunteer::hasOrdersLeft() const
{
    if (ordersLeft > 0)
        return true;
    else
        return false;
}

bool LimitedCollectorVolunteer::canTakeOrder(const Order &order) const
{
    return !isBusy();
}

void LimitedCollectorVolunteer::acceptOrder(const Order &order)
{
    activeOrderId = order.getId();
    set_timeleft(getCoolDown());
    decrease_ordersLeft();
}

string LimitedCollectorVolunteer::toString() const
{
    std::string orderid;
    std::string timeleft;
    if (getTimeLeft() == 0)
        timeleft = "None";
    else
        timeleft = std::to_string(getTimeLeft());

    if (activeOrderId == NO_ORDER)
        orderid = "None";
    else
        orderid = std::to_string(activeOrderId);

    std::string busyString = (isBusy()) ? "true" : "false";

    std::string output = "volunteer id " + std::to_string(getId()) + "\nisBusy: " + busyString +
                         "\n order id: " + orderid + "\n time left:" + timeleft + "\n orders left:" + std::to_string(ordersLeft);
    return output;
}

DriverVolunteer::DriverVolunteer(int id, const string &name, int maxDistance, int distancePerStep)
    : Volunteer(id, name), maxDistance(maxDistance),
      distancePerStep(distancePerStep), distanceLeft(0)
{
    role = 3;
    completedOrderId = NO_ORDER;
    activeOrderId = NO_ORDER;
}

int DriverVolunteer::getDistanceLeft() const
{
    return distanceLeft;
}

int DriverVolunteer::getMaxDistance() const
{
    return maxDistance;
}

int DriverVolunteer::getDistancePerStep() const
{
    return distancePerStep;
}

bool DriverVolunteer::decreaseDistanceLeft()
{
    distanceLeft = distanceLeft - distancePerStep;
    if (distanceLeft < 0)
        distanceLeft = 0;

    return distanceLeft == 0;
}

int DriverVolunteer::setdistance(int distance)
{
    distanceLeft = distance;
    return 0;
}

DriverVolunteer *DriverVolunteer::clone() const
{
    DriverVolunteer *output = new DriverVolunteer(*this);
    output->role = 3;
    return output;
}

bool DriverVolunteer::hasOrdersLeft() const
{
    return true;
}

bool DriverVolunteer::canTakeOrder(const Order &order) const
{
    return (!isBusy() && order.getdistance() <= maxDistance);
}

void DriverVolunteer::acceptOrder(const Order &order)
{
    distanceLeft = order.getdistance();
    activeOrderId = order.getId();
}

void DriverVolunteer::step()
{
    if (activeOrderId != -1)
        DriverVolunteer::decreaseDistanceLeft();
}

string DriverVolunteer::toString() const
{
    std::string orderid;
    std::string timeleft;
    if (getDistanceLeft() == 0)
        timeleft = "None";
    else
        timeleft = std::to_string(getDistanceLeft());

    if (activeOrderId == NO_ORDER)
        orderid = "None";
    else
        orderid = std::to_string(activeOrderId);

    std::string busyString = (isBusy()) ? "true" : "false";

    std::string output = "volunteer id " + std::to_string(getId()) + "\nisBusy: " + busyString +
                         "\n order id: " + orderid + "\n distance left:" + timeleft + "\n orders left: NO Limit";
    return output;
}

LimitedDriverVolunteer::LimitedDriverVolunteer(int id, const string &name, int maxDistance, int distancePerStep, int maxOrders)
    : DriverVolunteer(id, name, maxDistance, distancePerStep),
      maxOrders(maxOrders), ordersLeft(maxOrders)
{
    completedOrderId = NO_ORDER;
    activeOrderId = NO_ORDER;
    role = 4;
}

int LimitedDriverVolunteer::getMaxOrders() const
{
    return maxOrders;
}

int LimitedDriverVolunteer::getNumOrdersLeft() const
{
    return ordersLeft;
}

void LimitedDriverVolunteer::decrease_ordersLeft()
{
    ordersLeft = ordersLeft - 1;
}

LimitedDriverVolunteer *LimitedDriverVolunteer::clone() const
{
    LimitedDriverVolunteer *output = new LimitedDriverVolunteer(*this);
    output->role = 4;
    return output;
}

bool LimitedDriverVolunteer::hasOrdersLeft() const
{
    if (ordersLeft > 0)
        return true;
    else
        return false;
}

bool LimitedDriverVolunteer::canTakeOrder(const Order &order) const
{
    return !isBusy() && hasOrdersLeft() && order.getdistance() <= getMaxDistance();
}

void LimitedDriverVolunteer::acceptOrder(const Order &order)
{
    setdistance(order.getdistance());
    activeOrderId = order.getId();
    decrease_ordersLeft();
}

string LimitedDriverVolunteer::toString() const
{
    std::string orderid;
    std::string timeleft;
    if (getDistanceLeft() == 0)
        timeleft = "None";
    else
        timeleft = std::to_string(getDistanceLeft());

    if (activeOrderId == NO_ORDER)
        orderid = "None";
    else
        orderid = std::to_string(activeOrderId);

    std::string busyString = (isBusy()) ? "true" : "false";

    std::string output = "volunteer id " + std::to_string(getId()) + "\nisBusy: " + busyString +
                         "\n order id: " + orderid + "\n distance left:" + timeleft + "\n orders left: " + std::to_string(getNumOrdersLeft());
    return output;
}
