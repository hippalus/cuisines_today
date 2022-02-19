# Cuisines Registry

## The story:

Cuisines Registry is an important part of Book-That-Table Inc. backend stack. It keeps in memory customer preferences for restaurant cuisines and is accessed by a bunch of components to register and retrieve data. 


The first iteration of this component was implemented by rather inexperienced developer and now may require some cleaning while new functionality is being added. But fortunately, according to his words: "Everything should work and please keep the test coverage as high as I did"


## Your tasks:
1. **[Important!]** Adhere to the boy scout rule. Leave your code better than you found it.
It is ok to change any code as long as the CuisinesRegistry interface remains unchanged.
2. Make is possible for customers to follow more than one cuisine (return multiple cuisines in de.quandoo.recruitment.registry.api.CuisinesRegistry#customerCuisines)
3. Implement de.quandoo.recruitment.registry.api.CuisinesRegistry#topCuisines - returning list of most popular (highest number of registered customers) ones
4. Create a short write up on how you would plan to scale this component to be able to process in-memory billions of customers and millions of cuisines (Book-That-Table is already planning for galactic scale). (100 words max)

## Submitting your solution

+ Fork it to a **[!]**private**[!]** gitlab repository (go to `Settings -> General -> Visibility, project features, permissions -> Project visibility`).
+ Put the write up mentioned in point 4. into the end of this file.
+ Share the project with gitlab user *quandoo_recruitment_task* (go to `Settings -> Members -> Invite member`, find the user in `Select members to invite` and set `Choose a role permission` to `Developer`)
+ Send us an **ssh** clone link to the repository.

## Design and Refactoring

+ The benefit of having two duplicated caches is that it reduces the complexity of lookups to O(1), which is particularly handy
  when dealing with billions of data.
+ Implemented interface segregation and dependency inversion principle for provide high cohesion and loosely coupling.
+ Chose Port and Adapter Pattern for decoupling infrastructure and businesses rules.
+ Seperated 2 interface CuisineCustomersPort, CustomerCuisinesPort they provide relationship for Cuisine-Customers and
  Customer-Cuisines.
+ Contract with that interfaces for CuisinesRegistry.
+ If we use redis or hazelcast or ignite or memcache we need to implement that interfaces like adapter. It can be SQL or NOSQL
+ There are two implementations, first one is in memory adapter in which ConcurrentHashMap is used and the second is RedisAdapter in which distributed and scalable RMapCache is used
+ When we construct CuisinesRegistry if we passed RedisAdapter they run with redis.
+ When we construct CuisinesRegistry if we passed InMemoryAdapter they run with ConcurrentHashMap.
+ The max heap tree data structure is used to calculate the top cuisines in real time and also to keep them up to date, as well as
  to reduce the query cost.
+ In Java, to implement max heap tree structure, priority queue is used by reversing order.
+ To prevent cost of calculation in queue and duplication, top cuisines calculation is moved to registration phase in other words,
  this provides decrease in query cost
+ Gradle version is upgraded(v7.4) and java17(LTS) is used for development.

## Scaling

+ We should use some scalable in-memory data grid like Redis,Hazelcast,Ignite etc.
+ Ignite and Redis are able to persist data on disk, if we need it later. Also, they have partitioning and sharding.
+ Load balancers and service registry/discovery could be used to provide application level scaling. Also, kubernetes and container
  could be used for multi instance running.
+ Redundancy of all components must be provided with replications, clusters etc.
