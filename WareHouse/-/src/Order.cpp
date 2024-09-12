
#include "Order.h"

Order::Order(int id, int customerId, int distance) : id(id), customerId(customerId), distance(distance), status(OrderStatus::PENDING), collectorId(NO_VOLUNTEER),
                                                     driverId(NO_VOLUNTEER){

                                                     };

std::string Order::statusToString(OrderStatus status) const
{
    switch (status)
    {
    case OrderStatus::PENDING:
        return "PENDING";
    case OrderStatus::COLLECTING:
        return "COLLECTING";
    case OrderStatus::DELIVERING:
        return "DELEVERING";
    case OrderStatus::COMPLETED:
        return "COMPLETED";
    }
    return "";
}

int Order::getId() const
{
    return id;
}
int Order::getCustomerId() const
{
    return customerId;
}

void Order::setStatus(OrderStatus st)
{
    status = st;
}

void Order::setCollectorId(int collectorId)
{
    (*this).collectorId = collectorId;
}

void Order::setDriverId(int driverId)
{

    (*this).driverId = driverId;
}

int Order::getCollectorId() const
{

    return collectorId;
}

int Order::getDriverId() const
{

    return driverId;
}

OrderStatus Order::getStatus() const
{
    return status;
}
const string Order::toString() const
{

    std::string output = "OrderID " + std::to_string(getId()) + " ,CustomerID " + std::to_string(getCustomerId()) +
                         " ,OrderStatus " + (statusToString(getStatus()));
    return output;
}

int Order::getdistance() const
{
    return distance;
}
Order *Order::clone() const
{ // I added this
    return new Order(*this);
}
