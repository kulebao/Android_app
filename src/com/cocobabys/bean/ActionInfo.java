package com.cocobabys.bean;

public class ActionInfo extends BusinessInfo{
    private Price  price    = new Price();
    private String distance = "";

    public String getDistance(){
        return distance;
    }

    public void setDistance(String distance){
        this.distance = distance;
    }

    public Price getPrice(){
        return price;
    }

    public void setPrice(Price price){
        this.price = price;
    }

    public static class Price{
        private double origin     = 0.0;
        private double discounted = 0.0;

        public double getOrigin(){
            return origin;
        }

        public void setOrigin(double origin){
            this.origin = origin;
        }

        public double getDiscounted(){
            return discounted;
        }

        public void setDiscounted(double discounted){
            this.discounted = discounted;
        }

    }

}
